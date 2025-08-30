package com.gudrhs8304.ticketory.feature.pricing.domain;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.point.PricingKind;
import com.gudrhs8304.ticketory.feature.pricing.PricingOp;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_rule", indexes = {
        @Index(name = "idx_pricing_screen", columnList = "screen_id"),
        @Index(name = "idx_pricing_enabled", columnList = "enabled")
})
@Getter @Setter
public class PricingRule extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="screen_id", nullable = false)
    private Long screenId;

    @Enumerated(EnumType.STRING)
    @Column(name="kind", nullable = false)
    private PricingKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name="op", nullable = false)
    private PricingOp op;

    @Column(name="amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name="priority", nullable = false)
    private Integer priority = 100;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    @Column(nullable = false)
    private Boolean enabled = true;

    // 통화/단위: KRW 또는 %
    @Column(name = "currency", nullable = false, length = 8)
    private String currency = "KRW";

    @PrePersist
    @PreUpdate
    private void applyCurrencyByOp() {
        if (op == PricingOp.PCT_PLUS || op == PricingOp.PCT_MINUS) {
            this.currency = "%";
        } else {
            this.currency = "KRW";
        }
    }
}