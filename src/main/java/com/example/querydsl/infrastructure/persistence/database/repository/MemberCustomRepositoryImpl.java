package com.example.querydsl.infrastructure.persistence.database.repository;

import com.example.querydsl.ui.dto.request.MemberSearchConditionDto;
import com.example.querydsl.ui.dto.response.MemberTeamResponseDto;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.example.querydsl.infrastructure.persistence.database.entity.QMember.member;
import static com.example.querydsl.infrastructure.persistence.database.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class MemberCustomRepositoryImpl implements MemberCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTeamResponseDto> searchMembers(MemberSearchConditionDto conditionDto) {

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
                            usernameEq(conditionDto.getUsername()),
                            teamNameEq(conditionDto.getTeamName()),
                            userAgeGoe(conditionDto.getAgeGoe()),
                            userAgeLoe(conditionDto.getAgeLoe())
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

    /**
     * .fetchResults() 를 통해 데이터와 카운트를 동시에 조회
     * - 성능 상 좋진 않음.. -> deprecated
     * - 데이터와 카운트 쿼리를 분리하는 것 권고
     *
     * @param conditionDto
     * @param pageable
     * @return
     */
    @Override
    public Page<MemberTeamResponseDto> searchPageSimple(MemberSearchConditionDto conditionDto, Pageable pageable) {
        QueryResults<MemberTeamResponseDto> results = queryFactory
                .select(Projections.constructor(MemberTeamResponseDto.class,
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(conditionDto.getUsername()),
                        teamNameEq(conditionDto.getTeamName()),
                        userAgeGoe(conditionDto.getAgeGoe()),
                        userAgeLoe(conditionDto.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamResponseDto> content = results.getResults();
        long total = results.getTotal();

//        return new PageImpl<>(content, pageable, total);
        return PageableExecutionUtils.getPage(content, pageable, () -> total);
    }

    @Override
    public Page<MemberTeamResponseDto> searchPageComplex(MemberSearchConditionDto conditionDto, Pageable pageable) {
        /* 리뷰 목록 조회 count query 예제
          Long count = queryFactory.select(review.countDistinct())
               .from(review)
               .where(review.storeId.eq(storeId))
               .fetchOne();
         */

        // count query
        Long count = queryFactory.select(member.countDistinct())
                .from(member)
                .where(usernameEq(conditionDto.getUsername()),
                        teamNameEq(conditionDto.getTeamName()),
                        userAgeGoe(conditionDto.getAgeGoe()),
                        userAgeLoe(conditionDto.getAgeLoe())
                )
                .fetchOne();

        // content query
        List<MemberTeamResponseDto> content = queryFactory
                .select(Projections.constructor(MemberTeamResponseDto.class,
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(conditionDto.getUsername()),
                        teamNameEq(conditionDto.getTeamName()),
                        userAgeGoe(conditionDto.getAgeGoe()),
                        userAgeLoe(conditionDto.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> count);
    }
}
