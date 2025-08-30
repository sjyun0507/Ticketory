package com.gudrhs8304.ticketory.feature.refund;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.payment.Payment;
import com.gudrhs8304.ticketory.feature.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refund_log",
        indexes = @Index(name = "idx_refund_payment_time", columnList = "payment_id, created_at"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RefundLog extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    @Column(length = 255)
    private String reason;

    @Column(name = "pg_refund_tid", length = 100)
    private String pgRefundTid;

    @Column(name = "status", length = 20, nullable = false)
    private String status; // 'REQUESTED','DONE','FAILED' (간단히 문자열로. 원하면 Enum으로 분리)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_admin_id")
    private Member processedByAdmin;
}