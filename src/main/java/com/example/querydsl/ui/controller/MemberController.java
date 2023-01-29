package com.example.querydsl.ui.controller;

import com.example.querydsl.infrastructure.persistence.database.repository.MemberQuerydslRepository;
import com.example.querydsl.infrastructure.persistence.database.repository.MemberRepository;
import com.example.querydsl.ui.dto.request.MemberSearchConditionDto;
import com.example.querydsl.ui.dto.response.MemberTeamResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberQuerydslRepository memberQuerydslRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamResponseDto> searchMembersV1(MemberSearchConditionDto conditionDto) {
        return memberQuerydslRepository.searchByMemberSearchCondition(conditionDto);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamResponseDto> searchMembersV2(MemberSearchConditionDto conditionDto, Pageable pageable) {
        return memberRepository.searchPageSimple(conditionDto, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamResponseDto> searchMembersV3(MemberSearchConditionDto conditionDto, Pageable pageable) {
        return memberRepository.searchPageComplex(conditionDto, pageable);
    }
}
