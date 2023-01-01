package com.example.querydsl.infrastructure.persistence.database.entity;

import lombok.*;

import javax.persistence.*;

@Getter @Setter
@ToString(of = {"id", "username", "age"})  // ToString에서 연관관계도 넣을 경우 순환 참조 될 수 있음 !!
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")  // FK
    private Team team;


    @Builder
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        this.team = team;
        if (team != null) {
            changeTeam(team);
        }
    }

    public void changeTeam(Team team) {
        this.team = team;
        this.team.getMembers().add(this);
    }
}
