package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId); // 로그인 ID로 회원조회
    boolean existsByLoginId(String loginId); // 로그인 ID 존재 여부 확인
    boolean existsByEmail(String email); // 이메일 존재 여부 확인
    Optional<Member> findByEmail(String email);
    boolean existsByLoginIdIgnoreCase(String loginId);
}
