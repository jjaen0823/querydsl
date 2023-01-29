package com.example.querydsl.infrastructure.persistence.database.repository;

import com.example.querydsl.infrastructure.persistence.database.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    MemberQuerydslRepository memberQuerydslRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void jpaRepositoryTest() {
        Member member = Member.builder().username("member1").age(21).build();
        memberJpaRepository.save(member);

        // findById
        Optional<Member> findMemberById = memberJpaRepository.findById(member.getId());
        assertThat(findMemberById.get()).isEqualTo(member);

        // findAll
        List<Member> findMembers = memberJpaRepository.findAll();
        assertThat(findMembers).containsExactly(member);

        // findByUsername
        List<Member> findMembersByUsername = memberJpaRepository.findByUsername("member1");
        assertThat(findMembersByUsername).containsExactly(member);
    }

    @Test
    public void querydslRepositoryTest() {
        Member member = Member.builder().username("member1").age(21).build();
        memberQuerydslRepository.save(member);

        // findById
        Optional<Member> findMemberById = memberQuerydslRepository.findById(member.getId());
        assertThat(findMemberById.get()).isEqualTo(member);

        // findAll
        List<Member> findMembers = memberQuerydslRepository.findAll();
        assertThat(findMembers).containsExactly(member);

        // findByUsername
        List<Member> findMembersByUsername = memberQuerydslRepository.findByUsername("member1");
        assertThat(findMembersByUsername).containsExactly(member);
    }

    @Test
    public void existTest() {
        Member member = Member.builder().username("member1").age(21).build();
        memberQuerydslRepository.save(member);

        assertThat(memberQuerydslRepository.exist(1L)).isTrue();
        assertThat(memberQuerydslRepository.exist(2L)).isFalse();
    }

    @Test
    public void repositoryInterfaceTest() {
        Member member = Member.builder().username("member1").age(21).build();
        memberRepository.save(member);

        // findById
        Optional<Member> findMemberById = memberRepository.findById(member.getId());
        assertThat(findMemberById.get()).isEqualTo(member);

        // findAll
        List<Member> findMembers = memberRepository.findAll();
        assertThat(findMembers).containsExactly(member);

        // findByUsername
        List<Member> findMembersByUsername = memberRepository.findByUsername("member1");
        assertThat(findMembersByUsername).containsExactly(member);
    }

}