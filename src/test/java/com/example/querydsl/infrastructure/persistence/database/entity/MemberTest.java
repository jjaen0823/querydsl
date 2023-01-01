package com.example.querydsl.infrastructure.persistence.database.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void testEntity() {
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

        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember qMember = QMember.member;

        List<Member> members = query.selectFrom(qMember).fetch();
        members.forEach(member -> {
            System.out.println(member);
            System.out.println(member.getTeam().getName());
            System.out.println("========================================");
        });
    }

}