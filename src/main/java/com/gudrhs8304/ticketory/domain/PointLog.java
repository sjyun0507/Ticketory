package com.gudrhs8304.ticketory.domain;

import com.gudrhs8304.ticketory.domain.enums.PointChangeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PointLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 10)
    private PointChangeType changeType;

    @Column(name = "amount", nullable = false)
    private Integer amount;            // 부호 있음 (+적립 / –사용)

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;      // 이 변경 후 잔액

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
