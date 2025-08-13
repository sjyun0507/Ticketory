package com.gudrhs8304.ticketory.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cancel_log",
        indexes = @Index(name = "idx_cancel_booking_time", columnList = "booking_id, created_at"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CancelLog extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancel_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by_member_id")
    private Member canceledByMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canceled_by_admin_id")
    private Member canceledByAdmin;

    @Column(length = 255)
    private String reason;
}