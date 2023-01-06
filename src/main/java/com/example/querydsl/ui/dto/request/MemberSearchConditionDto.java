package com.example.querydsl.ui.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class MemberSearchConditionDto {
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;

    @Builder
    public MemberSearchConditionDto(String username, String teamName, Integer ageGoe, Integer ageLoe) {
        this.username = username;
        this.teamName = teamName;
        this.ageGoe = ageGoe;
        this.ageLoe = ageLoe;
    }
}
