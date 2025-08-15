package com.gudrhs8304.ticketory.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "cancel_log",
        indexes = {
                // ERD: (booking_id, created_at) 인덱스
                @Index(name = "idx_cancel_booking_created", columnList = "booking_id, created_at")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CancelLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancel_id")
    private Long cancelId;

    // 예약 FK (NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // 사용자 취소 주체(선택)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by_member_id")
    private Member canceledByMember;

    // 관리자 취소 주체(선택)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by_admin_id")
    private Member canceledByAdmin;

    @Column(length = 255)
    private String reason;

    /* ===== 편의 생성자 ===== */

    public static CancelLog ofMemberCancel(Booking booking, Long memberId, String reason) {
        return CancelLog.builder()
                .booking(booking)
                .canceledByMember(memberId == null ? null : new Member(memberId)) // 엔티티 로딩 없이 FK만 세팅
                .reason(reason)
                .build();
    }

    public static CancelLog ofAdminCancel(Booking booking, Long adminId, String reason) {
        return CancelLog.builder()
                .booking(booking)
                .canceledByAdmin(adminId == null ? null : new Member(adminId))
                .reason(reason)
                .build();
    }
}