// domain/SeatHold.java
package com.gudrhs8304.ticketory.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seat_hold",
        indexes = {
                @Index(name = "idx_hold_screening", columnList = "screening_id"),
                @Index(name = "idx_hold_expires", columnList = "expires_at")
        },
        uniqueConstraints = {
                // 같은 상영에서 같은 좌석을 중복 홀드 못 하도록(만료 정리는 별도)
                @UniqueConstraint(name = "uk_screening_seat", columnNames = {"screening_id","seat_id"})
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatHold {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hold_id")
    private Long holdId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "hold_key", length = 64)
    private String holdKey;

    // ⬇️ 추가: 홀드 유지시간(초)
    @Column(name = "hold_time", nullable = false)
    private Integer holdTime; // 예: 180초

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (holdTime == null) holdTime = 180;                   // 기본 3분
        if (expiresAt == null) expiresAt = LocalDateTime.now().plusSeconds(holdTime);
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}