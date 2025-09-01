package com.gudrhs8304.ticketory.feature.member;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member",
        indexes = {
                @Index(name = "idx_member_login_id", columnList = "login_id", unique = true),
                @Index(name = "idx_member_email", columnList = "email", unique = true),
                @Index(name = "idx_member_phone", columnList = "phone"),
                @Index(name = "idx_member_last_watched_at", columnList = "last_watched_at")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "login_id", length = 100, nullable = false, unique = true)
    private String loginId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 20)
    private String phone;

    /** 프로필 이미지 URL */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /** 마지막 관람(완료) 시각 */
    @Column(name = "last_watched_at")
    private LocalDate lastWatchedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_type", nullable = false)
    private SignupType signupType;

    @Column(name = "social_id", length = 100)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    @Column(name = "point_balance", nullable = false)
    private Integer pointBalance;

    private Boolean active = true;
    private LocalDateTime deletedAt;

    public Member(Long memberId) { this.memberId = memberId; }
}