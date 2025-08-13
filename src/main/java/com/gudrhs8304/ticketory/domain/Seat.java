package com.gudrhs8304.ticketory.domain;

import com.gudrhs8304.ticketory.domain.enums.SeatStatusType;
import com.gudrhs8304.ticketory.domain.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat",
        uniqueConstraints = @UniqueConstraint(name = "uk_seat_position", columnNames = {"screen_id","row_label","col_number"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Seat extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "row_label", length = 1, nullable = false)
    private String rowLabel;

    @Column(name = "col_number", nullable = false)
    private Integer colNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", length = 10, nullable = false)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private SeatStatusType status;
}