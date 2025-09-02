package com.gudrhs8304.ticketory.feature.refund.domain;

import com.gudrhs8304.ticketory.feature.refund.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_log",
        indexes = {
                @Index(name = "idx_refund_payment_time", columnList = "payment_id, created_at")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RefundLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "pg_refund_tid", length = 100)
    private String pgRefundTid;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RefundStatus status;   // DONE / FAILED

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "processed_by_admin_id")
    private Long processedByAdminId;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}