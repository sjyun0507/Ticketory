package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.enums.RoleType;
import com.gudrhs8304.ticketory.dto.MemberResponseDTO;
import com.gudrhs8304.ticketory.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    public record UpdateRoleRequest(@NotNull RoleType role) {}

    @Operation(summary = "회원 역할 변경(ADMIN 전용)")
    @PatchMapping("/{memberId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public MemberResponseDTO updateRole(
            @PathVariable Long memberId,
            @RequestBody UpdateRoleRequest req
    ) {
        return memberService.updateRole(memberId, req.role());
    }
}