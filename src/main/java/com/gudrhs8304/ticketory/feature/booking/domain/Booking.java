package com.gudrhs8304.ticketory.feature.booking.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.screening.Screening;
import com.gudrhs8304.ticketory.feature.booking.BookingPayStatus;
import com.gudrhs8304.ticketory.feature.member.Member;
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

    @Column(name = "qr_code_url", columnDefinition = "TEXT")
    private String qrCodeUrl;

    @Column(name = "is_send_alarm", nullable = false)
    private boolean isSendAlarm = false; // 기본값 false

    @PrePersist
    protected void onPrePersist() {
        if (this.bookingTime == null) {
            this.bookingTime = java.time.LocalDateTime.now();
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = BookingPayStatus.PENDING;
        }
    }

    @JsonIgnore
    public Member getMember() {
        return member;
    }
    @JsonIgnore
    public Screening getScreening() {
        return screening;
    }
}