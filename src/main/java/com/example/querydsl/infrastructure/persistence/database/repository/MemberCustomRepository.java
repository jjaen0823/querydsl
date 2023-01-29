package com.example.querydsl.infrastructure.persistence.database.repository;

import com.example.querydsl.ui.dto.request.MemberSearchConditionDto;
import com.example.querydsl.ui.dto.response.MemberTeamResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberCustomRepository {

    List<MemberTeamResponseDto> searchMembers(MemberSearchConditionDto conditionDto);

    /** Count Query 최적화
     * 1. page 시작이면서 content size가 page size 보다 작을 때
     * 2. page 마지막일 때 (offset + content size = total size)
     *
     * => PageableExecutionUtils.getPage 로 최적화 
     */
    Page<MemberTeamResponseDto> searchPageSimple(MemberSearchConditionDto conditionDto, Pageable pageable);
    Page<MemberTeamResponseDto> searchPageComplex(MemberSearchConditionDto conditionDto, Pageable pageable);
}
