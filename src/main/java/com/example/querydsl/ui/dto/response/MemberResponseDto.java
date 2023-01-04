package com.example.querydsl.ui.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {

    private String username;
    private int age;

    /** QueryProjection
     * [ 장점 ]
     * - compileQuerydsl -> QDto file 생성
     * - new QDto() 사용 가능
     * - compile 시점에 type check
     *
     * [ 단점 ]
     * - dto가 querydsl library에 의존성을 갖게 됨
     */
    @QueryProjection
    public MemberResponseDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
