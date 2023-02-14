package com.example.querydsl.infrastructure.persistence.database.repository;

import com.example.querydsl.infrastructure.persistence.database.entity.Member;
import com.example.querydsl.infrastructure.persistence.database.entity.QTeam;
import com.example.querydsl.infrastructure.persistence.database.repository.support.Querydsl4RepositorySupport;
import com.example.querydsl.ui.dto.request.MemberSearchConditionDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.example.querydsl.infrastructure.persistence.database.entity.QMember.member;
import static com.example.querydsl.infrastructure.persistence.database.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberSupportTestRepository extends Querydsl4RepositorySupport {
    public MemberSupportTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> searchPageBuAppluPage(MemberSearchConditionDto conditionDto, Pageable pageable) {
        JPAQuery<Member> query = selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(conditionDto.getUsername()),
                        teamNameEq(conditionDto.getTeamName()),
                        userAgeGoe(conditionDto.getAgeGoe()),
                        userAgeLoe(conditionDto.getAgeLoe())
                );

        List<Member> content = getQuerydsl().applyPagination(pageable, query).fetch();

        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    public Page<Member> applyPagination(MemberSearchConditionDto conditionDto, Pageable pageable) {
        /**
         * applyPagination 이 PageableExecutionUtils.getPage(content, pageable, query::fetchCount) 을 대신 해주는 것일 뿐
         */
        return applyPagination(pageable, query ->
                query.selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(conditionDto.getUsername()),
                                teamNameEq(conditionDto.getTeamName()),
                                userAgeGoe(conditionDto.getAgeGoe()),
                                userAgeLoe(conditionDto.getAgeLoe())
                        )
        );
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
