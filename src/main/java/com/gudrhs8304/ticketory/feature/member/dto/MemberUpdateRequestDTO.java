package com.gudrhs8304.ticketory.feature.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberUpdateRequestDTO {

    @Schema(description = "이름", example = "홍길동")
    @Size(min = 1, max = 50, message = "이름은 1~50자")
    private String name;

    @Schema(example = "010-1234-5678")
    @Pattern(
            regexp = "^(01[016789])(?:\\d{7,8}|-\\d{3,4}-\\d{4})$",
            message = "휴대폰 번호 형식이 올바르지 않습니다. 예: 010-1234-5678"
    )
    private String phone;

    @Schema(description = "이메일", example = "user@example.com")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    private String profileImageUrl;

    /** 변경할 비밀번호 (선택) */
    @Size(min=8, max=64, message="비밀번호는 8~64자")
    private String currentPassword;
    @Size(min=8, max=64, message="비밀번호는 8~64자")
    private String newPassword;
}
