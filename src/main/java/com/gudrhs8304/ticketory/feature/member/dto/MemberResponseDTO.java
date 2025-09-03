package com.gudrhs8304.ticketory.feature.member.dto;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.core.util.PhoneUtil;
import com.gudrhs8304.ticketory.feature.member.domain.Member;
import lombok.*;
/**
 * DTO는 데이터 전송을 위한 객체입니다.
 * 이 클래스는 API 응답 전용 MemberResponse DTO입니다.
 */

/**
 * MemberResponse DTO
 * -------------------
 * - 이 DTO는 서버 → 클라이언트로 전달되는 회원 응답 데이터 전용 클래스입니다.
 * - 비밀번호나 민감한 정보는 포함하지 않으며, API 응답 시 필요한 필드만 포함합니다.
 * - 주로 회원 정보 조회, 회원가입/수정 후 반환 데이터 등에 사용됩니다.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponseDTO extends BaseTimeEntity {
    /** 회원 고유 ID */
    private Long memberId;

    /** 로그인 ID (이메일 또는 소셜 ID) */
    private String loginId;

    /** 이름 */
    private String name;

    /** 이메일 */
    private String email;


    /** 핸드폰 */
    private String phone;

    /** 권한 (예: ROLE_USER, ROLE_ADMIN) */
    private String role;

    private Integer points;

    private String avatarUrl;

    public static MemberResponseDTO from(Member m) {
        return MemberResponseDTO.builder()
                .memberId(m.getMemberId())
                .email(m.getEmail())
                .loginId(m.getLoginId())
                .name(m.getName())
                .phone(PhoneUtil.format(m.getPhone())) // ← 여기서만 하이픈 붙임
                .role(m.getRole().name())
                .points(m.getPointBalance() == null ? 0 : m.getPointBalance())
                .avatarUrl(m.getAvatarUrl())
                .build();
    }
}
