package com.example.querydsl.infrastructure.persistence.database.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.hibernate.query.criteria.LiteralHandlingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static com.example.querydsl.infrastructure.persistence.database.entity.QMember.member;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBatchTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;  // 동시성 문제 신경쓰지 않아도 됨 ! -> 멀티스레딩 환경에서 문제 없이 동작하도록 설계되어 있음

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder().username("member1").age(21).team(teamA).build();
        Member member2 = Member.builder().username("member2").age(22).team(teamA).build();
        Member member3 = Member.builder().username("member3").age(23).team(teamA).build();
        Member member4 = Member.builder().username("member4").age(24).team(teamA).build();
        Member member5 = Member.builder().username("member5").age(25).team(teamB).build();
        Member member6 = Member.builder().username("member6").age(26).team(teamB).build();
        Member member7 = Member.builder().username("member7").age(27).team(teamB).build();
        Member member8 = Member.builder().username("member8").age(28).team(teamB).build();

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
        em.persist(member7);
        em.persist(member8);

//        em.flush();  // 영속성 컨텍스트에 있는 Object들의 실제 쿼리를 만들어서 DB에 flush
//        em.clear();  // 영속성 컨텍스트 초기화 -> 캐시 초기화
    }

    /** Bulk 연산
     * - 영속성 컨텍스트, 1차 cache 모두 무시되고 바로 DB 값을 변경
     *   : 영속성 컨텍스트, 1차 cache != DB
     * - bulk 연산 후에는 영속성 컨텍스트를 초기화 해주는 것이 좋음 !
     * - em.flush(), em.clear() 해주기 !!
     *
     */
    @Test
    public void bulkUpdateQuery() {
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(25))
                .execute();

        em.flush();
        em.clear();

        assertThat(count).isEqualTo(4);

        List<Member> results = queryFactory
                .selectFrom(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    public void bulkDeleteQuery() {
        long count = queryFactory
                .delete(member)
                .where(member.age.goe(25))
                .execute();

        assertThat(count).isEqualTo(4);
    }


}
