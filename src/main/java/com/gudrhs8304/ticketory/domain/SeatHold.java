package com.gudrhs8304.ticketory.domain;

import com.gudrhs8304.ticketory.domain.enums.HoldStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_hold",
        uniqueConstraints = @UniqueConstraint(name = "uk_hold_screening_seat", columnNames = {"screening_id","seat_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SeatHold extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hold_id")
    private Long holdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 비회원이면 null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private HoldStatus status;

    @Column(name = "hold_time", nullable = false)
    private LocalDateTime holdTime;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}