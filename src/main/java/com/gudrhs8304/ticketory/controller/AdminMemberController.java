package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.Member;
import com.gudrhs8304.ticketory.domain.enums.RoleType;
import com.gudrhs8304.ticketory.dto.AdminUpdateRoleRequestDTO;
import com.gudrhs8304.ticketory.dto.MemberResponseDTO;
import com.gudrhs8304.ticketory.dto.MemberRoleResponseDTO;
import com.gudrhs8304.ticketory.security.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.service.AdminMemberService;
import com.gudrhs8304.ticketory.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;
    private final AdminMemberService adminMemberService;

    public record UpdateRoleRequest(@NotNull RoleType role) {}

    @Operation(summary = "관리자 권한 변경", description = "관리자 전용 API — JWT에 ROLE_ADMIN이 있어야 접근 가능")
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<MemberRoleResponseDTO> updateRole(@PathVariable Long memberId,
                                                            @Valid @RequestBody AdminUpdateRoleRequestDTO req,
                                                            @AuthenticationPrincipal CustomUserPrincipal admin) {
        // acting admin id는 감사로그/정책에 쓰고 싶을 때 전달
        Long actingAdminId = (admin != null ? admin.getMemberId() : null);

        Member updated = adminMemberService.updateMemberRole(memberId, req.getRole(), actingAdminId);
        return ResponseEntity.ok(new MemberRoleResponseDTO(updated.getMemberId(), updated.getRole()));
    }
}