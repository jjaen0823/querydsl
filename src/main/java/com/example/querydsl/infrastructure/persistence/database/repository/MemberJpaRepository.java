package com.example.querydsl.infrastructure.persistence.database.repository;

import com.example.querydsl.infrastructure.persistence.database.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);

        return Optional.ofNullable(member);
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }
}
