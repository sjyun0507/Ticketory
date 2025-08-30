package com.gudrhs8304.ticketory.feature.admin.domain;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_action_log",
        indexes = {
                @Index(name = "idx_adminlog_created", columnList = "created_at"),
                @Index(name = "idx_adminlog_action", columnList = "action_type, target_table, target_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AdminActionLog extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_action_id")
    private Long adminActionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_member_id", nullable = false)
    private Member adminMember;

    @Column(name = "action_type", length = 40, nullable = false)
    private String actionType; // Enum으로 분리 가능

    @Column(name = "target_table", length = 100)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "payload_json", columnDefinition = "json")
    private String payloadJson;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;
}