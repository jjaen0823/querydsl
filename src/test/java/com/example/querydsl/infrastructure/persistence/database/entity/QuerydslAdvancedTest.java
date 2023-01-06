package com.example.querydsl.infrastructure.persistence.database.entity;

import com.example.querydsl.ui.dto.response.MemberResponseDto;
import com.example.querydsl.ui.dto.response.QMemberResponseDto;
import com.example.querydsl.ui.dto.response.UserResponseDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
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
public class QuerydslAdvancedTest {
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
    public void tupleProjection() {
        List<Tuple> results = queryFactory
                .select(member.username, member.age)  // projection -> Tuple 
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }
    
    @Test
    public void findDtoByJPQL() {
        // DTO package 명을 다 적어줘야 하고, 생성자(new) 방식만 사용할 수 있음
        List<MemberResponseDto> results = em.createQuery(
                "select new com.example.querydsl.ui.dto.response.MemberResponseDto(m.username, m.age)" +
                        " from Member m",
                        MemberResponseDto.class)
                .getResultList();

        results.forEach(System.out::println);
    }
    
    @Test
    public void findDtoByQuerydsl_setter() {
        List<MemberResponseDto> results = queryFactory
                .select(Projections.bean(MemberResponseDto.class,  // need @NoArgsConstructor, @Setter
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    public void findDtoByQuerydsl_field() {
        List<MemberResponseDto> results = queryFactory
                .select(Projections.fields(MemberResponseDto.class,  // don't need @Getter, @Setter (field injection)
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    public void findDtoByQuerydsl_constructor() {
        List<MemberResponseDto> results = queryFactory
                .select(Projections.constructor(MemberResponseDto.class,  // need @AllArgsConstructor
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    public void findUserDtoByQuerydsl_field_as() {
        List<UserResponseDto> results = queryFactory
                .select(Projections.fields(UserResponseDto.class,  // don't need @Getter, @Setter (field injection)
                        member.username.as("name"),  // name: field 이름이 다르면 null로 들어감 -> alias 사용
                        member.age  // age
                ))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    public void findUserDtoQuerydsl_field_subQuery_as() {
        QMember subMember = new QMember("subMember");

        /** ExpressionUtils.as
         * - field, subQuery alias 사용
         * - field alias 는 .as() 사용하는 것 권장
         */
        List<UserResponseDto> results = queryFactory
                .select(Projections.fields(UserResponseDto.class,  // don't need @Getter, @Setter (field injection)
                        member.username.as("name"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(subMember.age.max())
                                        .from(subMember),
                                "age"
                        )
                ))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }

    @Test
    public void findUserDtoByQuerydsl_constructor() {
        List<UserResponseDto> results = queryFactory
                .select(Projections.constructor(UserResponseDto.class,  // need @AllArgsConstructor
                        member.username,  // field 명이 달라도 data type만 동일하면 사용 가능
                        member.age
                ))
                .from(member)
                .fetch();

        results.forEach(System.out::println);
    }
    
    @Test
    public void findDtoByQueryProjection() {
        MemberResponseDto dto = queryFactory
                .select(new QMemberResponseDto(member.username, member.age))
                .from(member)
                .fetchFirst();

        assertThat(dto.getUsername()).isEqualTo("member1");
        assertThat(dto.getAge()).isEqualTo(21);
    }

    @Test
    public void sqlFunction_replace() {
        List<String> results = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "Member"
                ))
                .from(member)
                .fetch();

        assertThat(results.stream().allMatch(s -> s.startsWith("Member"))).isTrue();
    }

    @Test
    public void sqlFunction_lower() {
        List<String> results = queryFactory
//                .select(Expressions.stringTemplate(
//                        "function('upper', {0})",
//                        member.username
//                ))
                .select(member.username.upper())
                .from(member)
                .fetch();

        assertThat(results.stream().allMatch(s -> s.startsWith("member".toUpperCase()))).isTrue();
    }


}
