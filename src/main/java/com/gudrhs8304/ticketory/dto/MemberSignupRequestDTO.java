package com.gudrhs8304.ticketory.dto;

import com.gudrhs8304.ticketory.domain.BaseTimeEntity;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 회원 가입 요청 DTO
 * -------------------
 * - 클라이언트 → 서버로 전달되는 회원가입 요청 데이터를 담는 클래스
 * - 비밀번호, 이메일, 이름, 닉네임 등 가입에 필요한 정보만 포함
 * - @Valid 어노테이션으로 컨트롤러 단에서 유효성 검증 수행
 */

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberSignupRequestDTO extends BaseTimeEntity {
    /** 회원 이메일 (로그인 ID로 사용) */
    @NotBlank @Email
    private String email;

    /** 회원 이름 */
    @NotBlank
    private String name;

    /** 비밀번호 (BCrypt 인코딩 전 원문) */
    @NotBlank @Size(min=8, max=64)
    private String password;

    /** 닉네임 (선택) */
    private String nickname;

    /** 핸드폰 */
    private String phone;


}