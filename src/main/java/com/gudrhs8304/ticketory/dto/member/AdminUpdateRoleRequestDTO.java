package com.gudrhs8304.ticketory.dto.member;

import com.gudrhs8304.ticketory.domain.enums.RoleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUpdateRoleRequestDTO {
    @NotNull
    private RoleType role; // USER 또는 ADMIN
}
