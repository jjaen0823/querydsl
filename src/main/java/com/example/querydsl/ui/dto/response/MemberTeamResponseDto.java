package com.example.querydsl.ui.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberTeamResponseDto {

    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;
}
