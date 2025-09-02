package com.gudrhs8304.ticketory.feature.member.controller;

import com.gudrhs8304.ticketory.feature.member.service.MemberQueryService;
import com.gudrhs8304.ticketory.feature.member.dto.MemberProfileRes;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberStoryProfileController {

    private final MemberQueryService memberQueryService;

    @Operation(summary = "스토리 프로필(내 정보)", description = "{ memberId, name, avatarUrl, lastWatchedAt }")
    @GetMapping("/me")
    public MemberProfileRes me(@AuthenticationPrincipal(expression = "memberId") Long memberId) {
        return memberQueryService.getMyProfile(memberId);
    }
}
