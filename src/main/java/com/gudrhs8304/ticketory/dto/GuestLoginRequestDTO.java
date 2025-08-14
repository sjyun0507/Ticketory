package com.gudrhs8304.ticketory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuestLoginRequestDTO {
    @Schema(description = "로그인 이메일", example = "guest@example.com")
    @Email
    @NotBlank
    String email;

    @Schema(description = "비밀번호(신규 생성 시에도 사용)", example = "P@ssw0rd!")
    @NotBlank
    String password;



    @Schema(description = "전화번호", example = "010-7253-3804")
    String phone;

}
