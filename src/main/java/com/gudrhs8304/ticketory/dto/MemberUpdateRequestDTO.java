package com.gudrhs8304.ticketory.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberUpdateRequestDTO {

    /** 변경할 닉네임 */
    private String nickname;

    @Pattern(regexp = "^!01[016789]-?\\d{3,4}-?\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phone;

    private String profileImageUrl;

    /** 변경할 비밀번호 (선택) */
    @Size(min=8, max=64, message="비밀번호는 8~64자")
    private String currentPassword;
    @Size(min=8, max=64, message="비밀번호는 8~64자")
    private String newPassword;
}
