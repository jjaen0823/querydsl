package com.example.querydsl.infrastructure.persistence.database.entity;

import com.example.querydsl.infrastructure.persistence.database.repository.MemberQuerydslRepository;
import com.example.querydsl.ui.dto.request.MemberSearchConditionDto;
import com.example.querydsl.ui.dto.response.MemberTeamResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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
public class QuerydslDynamicTest {
    @Autowired
    EntityManager em;

    @Autowired
    JPAQueryFactory queryFactory;  // 동시성 문제 신경쓰지 않아도 됨 ! -> 멀티스레딩 환경에서 문제 없이 동작하도록 설계되어 있음

    @Autowired
    MemberQuerydslRepository memberQuerydslRepository;


    @BeforeEach
    public void before() {
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
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 21;

        List<Member> result = searchMember1(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    /** 동적 쿼리
     * 1. BooleanBuilder
     * - 생성자 안에 조건을 넣으면 default 조건으로 사용할 수 있음
     *
     * 2. * Where Parameter
     * - 조건을 method로 작성
     *   - 가독성 증가, method 재사용
     *   - null 체크 필수
     *   ex) return param != null ? member.username.eq(param) : null;
     *
     */
    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (usernameParam != null) {
            booleanBuilder.and(member.username.eq(usernameParam));
        }
        if (ageParam != null) {
            booleanBuilder.and(member.age.eq(ageParam));
        }

        return queryFactory
                .selectFrom(member)
                .where(booleanBuilder)
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 21;

        List<Member> result = searchMember2(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return queryFactory
                .selectFrom(member)
                //.where(usernameEq(usernameParam), ageEq(ageParam))  // where 절에서 null 무시 됨
                .where(allEq(usernameParam, ageParam))  // where 절에서 null 무시 됨
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameParam) {
        return usernameParam != null ? member.username.eq(usernameParam) : null;
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    private BooleanExpression allEq(String usernameParam, Integer ageParam) {
        return usernameEq(usernameParam).and(ageEq(ageParam));
    }
    
    @Test
    public void memberSearchConditionTest() {
        List<MemberTeamResponseDto> memberTeamResponseDtoList = memberQuerydslRepository.searchByMemberSearchCondition(
                MemberSearchConditionDto.builder()
                        .teamName("teamA")
                        .ageGoe(21)
                        .build()
        );

        assertThat(memberTeamResponseDtoList.size()).isEqualTo(2);
    }
    
}
