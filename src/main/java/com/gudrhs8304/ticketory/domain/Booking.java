package com.gudrhs8304.ticketory.domain;

import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking",
        indexes = {
                @Index(name = "idx_booking_member", columnList = "member_id"),
                @Index(name = "idx_booking_screening", columnList = "screening_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Booking extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 비회원은 null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 10, nullable = false)
    private BookingPayStatus paymentStatus;

    @Column(name = "qr_code_url", length = 255)
    private String qrCodeUrl;
}