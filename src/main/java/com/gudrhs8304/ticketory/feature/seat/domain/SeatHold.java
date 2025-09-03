package com.gudrhs8304.ticketory.feature.seat.domain;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.screening.domain.Screening;

import com.gudrhs8304.ticketory.feature.seat.enums.HoldStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seat_hold",
        indexes = {
                @Index(name = "idx_hold_screening", columnList = "screening_id"),
                @Index(name = "idx_hold_expires",   columnList = "expires_at")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SeatHold extends BaseTimeEntity {

    @CreatedDate
    protected LocalDateTime createdAt;
    @LastModifiedDate
    protected LocalDateTime updatedAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hold_id")
    private Long holdId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "hold_key", length = 64)
    private String holdKey;

    // JPA 레벨에서 NULL 금지. (DB도 DEFAULT 120 권장: 아래 DDL 참고)
    @Column(
            name = "hold_time",
            nullable = false
            // 필요시 DB 기본값까지 지정하려면 다음 줄을 사용 (MariaDB 의존적)
            // , columnDefinition = "INT NOT NULL DEFAULT 120"
    )
    private Integer holdTime; // seconds

    private static final int DEFAULT_HOLD_SECONDS = 120;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HoldStatus status;

    @PrePersist
    protected void prePersistSeatHold() {
        if (holdTime == null) holdTime = DEFAULT_HOLD_SECONDS;
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusSeconds(holdTime);
        }
        if (status == null) status = HoldStatus.HOLD;
        // createdAt/updatedAt은 BaseTimeEntity(@CreatedDate/@LastModifiedDate)가 처리
    }
}