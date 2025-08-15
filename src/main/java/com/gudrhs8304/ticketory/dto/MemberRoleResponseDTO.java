package com.gudrhs8304.ticketory.dto;

import com.gudrhs8304.ticketory.domain.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRoleResponseDTO {
    private Long memberId;
    private RoleType role;
}
