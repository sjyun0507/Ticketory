package com.gudrhs8304.ticketory.feature.payment.domain;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.payment.enums.PaymentProvider;
import com.gudrhs8304.ticketory.feature.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment",
        indexes = {
                @Index(name = "uk_provider_tx_id", columnList = "provider_tx_id", unique = true)
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 10)
    private PaymentProvider provider;

    /**
     * 실제 결제 금액 (PG로 결제한 금액)
     * → 총액 - 사용포인트
     */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(unique = true, nullable = false)
    private String orderId;

    @Column(name = "provider_tx_id", length = 64)
    private String providerTxId;


    // ▼ 포인트 관련은 DB 컬럼 X → 계산/DTO에서만 사용
    @Transient
    private Integer pointsUsed;   // 총액 - amount 로 계산
}