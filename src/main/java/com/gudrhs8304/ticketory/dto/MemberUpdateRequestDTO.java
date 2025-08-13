package com.gudrhs8304.ticketory.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberUpdateRequestDTO {
    /** 변경할 이름 */
    private String name;

    /** 변경할 닉네임 */
    private String nickname;

    /** 변경할 비밀번호 (선택) */
    @Size(min=8, max=64, message="비밀번호는 8~64자")
    private String password;
}
