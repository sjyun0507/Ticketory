package com.gudrhs8304.ticketory.feature.seat;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.screen.Screen;
import com.gudrhs8304.ticketory.feature.seat.SeatStatus;
import com.gudrhs8304.ticketory.feature.seat.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "seat",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_seat_position",
                columnNames = {"screen_id","row_label","col_number"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Seat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    // CHAR(1)
    @Column(name = "row_label", length = 1, nullable = false)
    private String rowLabel;

    // INT NOT NULL
    @Column(name = "col_number", nullable = false)
    private Integer colNumber;

    // ENUM('NORMAL','VIP') DEFAULT 'NORMAL'
    @Enumerated(EnumType.STRING)
    @Column(
            name = "seat_type",
            length = 10,
            nullable = false,
            columnDefinition = "ENUM('NORMAL','VIP') DEFAULT 'NORMAL'"
    )
    private SeatType seatType;

    // ENUM('AVAILABLE','PENDING','BOOKED') DEFAULT 'AVAILABLE'
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            length = 20,
            nullable = false,
            columnDefinition = "ENUM('AVAILABLE','DISABLED') DEFAULT 'AVAILABLE'"
    )
    private SeatStatus status;

    /** JPA로 null 넣을 때도 기본값 보장 */
    @PrePersist
    void applyDefaults() {
        if (seatType == null) seatType = SeatType.NORMAL;
        if (status == null) status = SeatStatus.AVAILABLE;
    }
}