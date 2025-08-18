package com.gudrhs8304.ticketory.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 로그인 요청 DTO
 * -------------------
 * - 클라이언트 → 서버로 전달되는 로그인 요청 데이터
 * - 일반 로그인 시 loginId = 이메일, 소셜 로그인 시 소셜 ID 사용
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberLoginRequestDTO {
    /** 로그인 ID (이메일) */
    @NotBlank
    @Schema(example = "test2@test.com")
    private String loginId;

    /** 비밀번호 */
    @NotBlank
    @Schema(example = "12345678")
    private String password;
}