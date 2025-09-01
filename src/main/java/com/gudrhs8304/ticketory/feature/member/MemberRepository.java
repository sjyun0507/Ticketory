package com.gudrhs8304.ticketory.feature.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId); // 로그인 ID로 회원조회
    boolean existsByLoginId(String loginId); // 로그인 ID 존재 여부 확인
    boolean existsByEmail(String email); // 이메일 존재 여부 확인
    Optional<Member> findByEmail(String email);
    boolean existsByLoginIdIgnoreCase(String loginId);

    @Query("select m.memberId from Member m where m.loginId = :loginId")
    Optional<Long> findIdByLoginId(@Param("loginId") String loginId);

    long countByRole(RoleType role);

    boolean existsByEmailAndMemberIdNot(String email, Long memberId);

    /** last_watched_at < endDate 일 때만 갱신 (DATE 비교) */
    @Modifying
    @Query("""
       UPDATE Member m
          SET m.lastWatchedAt = :endDate
        WHERE m.memberId = :memberId
          AND (m.lastWatchedAt IS NULL OR m.lastWatchedAt < :endDate)
    """)
    int updateLastWatchedIfNewer(@Param("memberId") Long memberId,
                                 @Param("endDate") LocalDate endDate);

    /** 전체 재계산 (native, DATE로 집계) */
    @Modifying
    @Query(value = """
        UPDATE member m
        JOIN (
          SELECT b.member_id          AS member_id,
                 MAX(DATE(s.end_at))  AS last_date
            FROM booking b
            JOIN screening s ON s.screening_id = b.screening_id
           WHERE b.payment_status = 'PAID'
             AND s.end_at <= NOW(6)
           GROUP BY b.member_id
        ) x ON x.member_id = m.member_id
        SET m.last_watched_at = x.last_date
    """, nativeQuery = true)
    int recomputeLastWatchedForAll();

    /** 특정 회원만 재계산 (native, DATE) */
    @Modifying
    @Query(value = """
        UPDATE member m
        JOIN (
          SELECT b.member_id          AS member_id,
                 MAX(DATE(s.end_at))  AS last_date
            FROM booking b
            JOIN screening s ON s.screening_id = b.screening_id
           WHERE b.payment_status = 'PAID'
             AND s.end_at <= NOW(6)
             AND b.member_id = :memberId
           GROUP BY b.member_id
        ) x ON x.member_id = m.member_id
        SET m.last_watched_at = x.last_date
    """, nativeQuery = true)
    int recomputeLastWatchedForMember(@Param("memberId") Long memberId);
}

