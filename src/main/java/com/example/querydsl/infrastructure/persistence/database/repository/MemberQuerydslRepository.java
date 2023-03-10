package com.example.querydsl.infrastructure.persistence.database.repository;

import com.example.querydsl.infrastructure.persistence.database.entity.Member;
import com.example.querydsl.ui.dto.request.MemberSearchConditionDto;
import com.example.querydsl.ui.dto.response.MemberTeamResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.management.Query;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.example.querydsl.infrastructure.persistence.database.entity.QMember.member;
import static com.example.querydsl.infrastructure.persistence.database.entity.QTeam.team;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;


/**
 * 1. 묵시적 join 은 cross join을 발생 -> 명시적 join (innerJoin)
 * 2. select 절에 Entity 조회 자제 -> id 값만 가져오는 등으로 변경
 * 3. Group By 최21적화 -> OrderByNull 사용 (조회 결과 100건 이하일 경우 WAS에서 정렬)
 * 4. 무분별한 DirtyChecking -> Bulk Update 사용 (이 경우는 1차, 2차 cache가 갱신되지 않으므로 flush, clear 해줘야 함)
 */

@Transactional
@Repository
@RequiredArgsConstructor
public class MemberQuerydslRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public void save(Member member) {
        em.persist(member);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(member)
                        .where(member.id.eq(id))
                        .fetchOne()
        );
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Member> findByUsername(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    /** exist
     * - exist 대신 limit(1) 사용
     */
    @Transactional(readOnly = true)
    public boolean exist(Long memberId) {
        Integer fetchFirst = queryFactory
                .selectOne()
                .from(member)
                .where(member.id.eq(memberId))
                .fetchFirst();

        return fetchFirst != null;
    }

    public List<MemberTeamResponseDto> searchByMemberSearchCondition(MemberSearchConditionDto condition) {

        return queryFactory
                .select(Projections.constructor(MemberTeamResponseDto.class,
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        userAgeGoe(condition.getAgeGoe()),
                        userAgeLoe(condition.getAgeLoe())
//                        memberSearchCondition(condition)
                )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression userAgeGoe(Integer age) {
        return age != null ? member.age.goe(age) : null;
    }

    private BooleanExpression userAgeLoe(Integer age) {
        return age != null ? member.age.loe(age) : null;
    }

    private BooleanExpression ageBetween(Integer ageGoe, Integer ageLoe) {
        return userAgeGoe(ageGoe).and(userAgeLoe(ageLoe));
    }

    private BooleanExpression memberSearchCondition(MemberSearchConditionDto condition) {
//        BooleanBuilder booleanBuilder = new BooleanBuilder();
//        booleanBuilder.
        return usernameEq(condition.getUsername())
                .and(teamNameEq(condition.getTeamName()))  // NPE -> usernameEq이 Null 이면 .and() method가 없기 떄문에 NPE 발생 -> 첫 번째 조건만 null이 아니면 됨
                .and(userAgeGoe(condition.getAgeGoe()))
                .and(userAgeLoe(condition.getAgeLoe()));
    }

}
