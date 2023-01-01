package com.example.querydsl;

import com.example.querydsl.infrastructure.persistence.database.entity.Member;
import com.example.querydsl.infrastructure.persistence.database.entity.QMember;
import com.example.querydsl.infrastructure.persistence.database.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static com.example.querydsl.infrastructure.persistence.database.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

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
        Member member3 = Member.builder().username("member3").age(23).team(teamB).build();
        Member member4 = Member.builder().username("member4").age(24).team(teamB).build();

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();  // 영속성 컨텍스트에 있는 Object들의 실제 쿼리를 만들어서 DB에 flush
        em.clear();  // 영속성 컨텍스트 초기화 -> 캐시 초기화
    }

    @Test
    public void startJPQL() {
        Member findByJPQL = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findByJPQL.getUsername()).isSameAs("member1");
    }

    // Querydsl은 Compile Error로 잡을 수 있다.
    @Test
    public void startQuerydsl() {
        // 같은 table을 join 해야 하는 경우 -> alias 변경해서 사용 !!
        QMember m1 = new QMember("m1");

        Member findMember = queryFactory
                .select(member)  // QMember.member를 static으로 import 해서 사용함 (권장)
                .from(member)
                .where(member.username.eq("member1"))  // Parameter Binding
                .fetchOne();


        assertThat(findMember).isNotNull();
        assertThat(findMember.getUsername()).isSameAs("member1");

    }
}
