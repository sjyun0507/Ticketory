package com.gudrhs8304.ticketory.dto.member;

import lombok.*;

/**
 * JWT 응답 DTO
 * -------------------
 * - 로그인 성공 시 발급되는 토큰 정보
 * - accessToken과 tokenType("Bearer") 포함
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponseDTO {
    /** 발급된 JWT 액세스 토큰 */
    private String accessToken;

    /** 토큰 타입 (Bearer 고정) */
    private String tokenType;
}
