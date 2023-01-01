package com.example.querydsl;

import com.example.querydsl.infrastructure.persistence.database.entity.Member;
import com.example.querydsl.infrastructure.persistence.database.entity.QMember;
import com.example.querydsl.infrastructure.persistence.database.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional  // Test에 @Transactional 이 있는 경우 -> Rollback
//@Commit
class QuerydslApplicationTests {

    @Autowired  // or @PersistenceContext
    EntityManager em;

    @Test
    void memberTest() {
        Team teamA = Team.builder().name("A").build();
        Member member1 = Member.builder().username("jjaen").age(25).team(teamA).build();
        Member member2 = Member.builder().username("jjaem").age(28).team(teamA).build();
        em.persist(member1);
        em.persist(member2);

        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember qMember = new QMember("m");  // variable

        List<Member> members = query.selectFrom(qMember).fetch();

        Assertions.assertThat(members.size()).isSameAs(2);
    }

}
