package com.gudrhs8304.ticketory.domain;

import com.gudrhs8304.ticketory.domain.enums.PaymentProvider;
import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 10)
    private PaymentProvider provider;

    @Column(name = "provider_tx_id", length = 100, nullable = false, unique = true)
    private String providerTxId;

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
}