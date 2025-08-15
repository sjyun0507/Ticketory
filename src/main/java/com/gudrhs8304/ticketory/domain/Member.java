package com.gudrhs8304.ticketory.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gudrhs8304.ticketory.domain.enums.RoleType;
import com.gudrhs8304.ticketory.domain.enums.SignupType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member",
        indexes = {
                @Index(name = "idx_member_login_id", columnList = "login_id", unique = true),
                @Index(name = "idx_member_email", columnList = "email", unique = true),
                @Index(name = "idx_member_phone", columnList = "phone")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_type", nullable = false)
    private SignupType signupType;

    @Column(name = "social_id", length = 100)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "point_balance", nullable = false)
    private Integer pointBalance;

    private Boolean active = true;            // 기본 true
    private LocalDateTime deletedAt;

    public Member(Long memberId) {
        this.memberId = memberId;
    }



}