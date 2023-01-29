package com.example.querydsl.infrastructure.persistence.database.repository;

import com.example.querydsl.ui.dto.request.MemberSearchConditionDto;
import com.example.querydsl.ui.dto.response.MemberTeamResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.querydsl.infrastructure.persistence.database.entity.QMember.member;
import static com.example.querydsl.infrastructure.persistence.database.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특화된 쿼리문 같은 경우는 Repository class를 따로 생성해서 개별적으로 놔둬도 된다 !
     * @param conditionDto : 검색 조건
     * @return : 검색 조건에 따른 Member dto list
     */
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
}
