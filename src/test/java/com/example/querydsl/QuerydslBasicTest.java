package com.example.querydsl;

import com.example.querydsl.infrastructure.persistence.database.entity.Member;
import com.example.querydsl.infrastructure.persistence.database.entity.QMember;
import com.example.querydsl.infrastructure.persistence.database.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;
import java.util.List;

import static com.example.querydsl.infrastructure.persistence.database.entity.QMember.member;
import static com.example.querydsl.infrastructure.persistence.database.entity.QTeam.team;
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

    @Test
    public void searchQuerydsl() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),  // ,(콤마) == .and
                        (member.age.eq(21))
                )
                .fetch();

        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    public void resultFetch() {
        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member2"))
                .fetchOne();

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

    }

    @Test
    public void sort() {
        em.persist(Member.builder().age(25).build());
        em.persist(Member.builder().username("member5").age(25).build());
        em.persist(Member.builder().username("member6").age(26).build());

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.goe(25))
                .orderBy(
                        member.age.desc(),
                        member.username.asc().nullsLast()
                )
                .fetch();

        Member member6 = members.get(0);
        Member member5 = members.get(1);
        Member member4 = members.get(2);
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member4.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        // Tuple보다 Dto로 직접 뽑아서 조회하는 경우가 많음
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.avg()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.avg())).isEqualTo(22.5);


    }
    
    @Test
    public void groupBy() {
        List<Tuple> results = queryFactory
                .select(
                        member.team.name,
                        member.age.avg()
                )
                .from(member)
                .groupBy(member.team)
                .fetch();

        Tuple teamA = results.get(0);
        Tuple teamB = results.get(1);

        assertThat(teamA.get(member.team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(21.5);

        assertThat(teamB.get(member.team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(23.5);
    }

    @Test
    public void join() {
        List<Member> members = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(members)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
    * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
    * */
    @Test
    public void join_on_filtering() {
        List<Tuple> results = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))  // outer join -> on
                // .where(team.name.eq("teamA"))  // inner join -> where
                .fetch();

        results.forEach(System.out::println);
        /** leftJoin
         * [Member(id=1, username=member1, age=21), Team(id=1, name=teamA)]
         * [Member(id=2, username=member2, age=22), Team(id=1, name=teamA)]
         * [Member(id=3, username=member3, age=23), null]
         * [Member(id=4, username=member4, age=24), null]
         */
        /** rightJoin
         * [Member(id=1, username=member1, age=21), Team(id=1, name=teamA)]
         * [Member(id=2, username=member2, age=22), Team(id=1, name=teamA)]
         * [null, Team(id=2, name=teamB)]
         */
    }

    @Test
    public void join_on_no_relation() {
        em.persist(Member.builder().username("teamA").age(26).build());
        em.persist(Member.builder().username("teamB").age(27).build());
        em.persist(Member.builder().username("teamC").age(28).build());

        List<Tuple> results = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))  // on join: .from(member).leftJoin(team).on(XXX)
                .fetch();                                          // 일반 join: .leftJoin(member.team, team)

        results.forEach(System.out::println);
        /**
         * [Member(id=1, username=member1, age=21), null]
         * [Member(id=2, username=member2, age=22), null]
         * [Member(id=3, username=member3, age=23), null]
         * [Member(id=4, username=member4, age=24), null]
         * [Member(id=5, username=teamA, age=26), Team(id=1, name=teamA)]
         * [Member(id=6, username=teamB, age=27), Team(id=2, name=teamB)]
         * [Member(id=7, username=teamC, age=28), null]
         */
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void no_fetchJoin() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(isLoaded).as("no fetch join").isFalse();
    }

    @Test
    public void fetchJoin() {
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()  // fetchJoin: 연관된 team 엔티티도 한꺼번에 가져옴
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(isLoaded).as("no fetch join").isTrue();
    }

    /**
     * sub  Query - JPAExpressions 사용
     */
    @Test
    public void subQuery() {
        QMember subMember = new QMember("subMember");

        Member result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(subMember.age.max())
                                .from(subMember)
                )).fetchOne();

        assertThat(result.getAge()).isEqualTo(24);
    }

    @Test
    public void subQuery_goe() {
        QMember subMember = new QMember("subMember");

        List<Member> results = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember)
                )).fetch();

        assertThat(results).extracting("age")
                .containsExactly(23, 24);
    }

    @Test
    public void subQuery_in() {
        QMember subMember = new QMember("subMember");

        List<Member> results = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(subMember.age)
                                .from(subMember)
                                .where(subMember.age.gt(22))
                )).fetch();

        assertThat(results).extracting("age")
                .containsExactly(23, 24);
    }
    
    @Test
    public void basicCase() {
        List<String> results = queryFactory
                .select(member.age
                        .when(21).then("스물하나")
                        .when(22).then("스물둘")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();
        results.forEach(System.out::println);
    }

    @Test
    public void complexCase() {
        List<String> results = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(10, 19)).then("10대")
                        .when(member.age.between(20, 29)).then("20대")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    public void constant() {
        List<Tuple> results = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }
}
