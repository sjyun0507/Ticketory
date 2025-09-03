package com.gudrhs8304.ticketory.feature.booking.domain;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.screening.domain.Screening;

import com.gudrhs8304.ticketory.feature.seat.domain.Seat;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_seat",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_booking_seat", columnNames = {"booking_id", "seat_id"}),
                @UniqueConstraint(name = "uk_screening_seat", columnNames = {"screening_id", "seat_id"})
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BookingSeat extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_seat_id")
    private Long bookingSeatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;
}