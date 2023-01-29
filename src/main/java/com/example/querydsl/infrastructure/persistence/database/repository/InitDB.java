package com.example.querydsl.infrastructure.persistence.database.repository;

import com.example.querydsl.infrastructure.persistence.database.entity.Member;
import com.example.querydsl.infrastructure.persistence.database.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitDB {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {

        @PersistenceContext
        private EntityManager em;

        /**
         * spring life cycle 때문에
         * @PostConstructor와 @Transactional을 분리 해야 함
         */
        @Transactional
        public void init() {
            Team teamA = Team.builder().name("teamA").build();
            Team teamB = Team.builder().name("teamB").build();

            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;

                em.persist(Member.builder().username("member"+i).age(i).team(selectedTeam).build());
            }
        }
    }
}
