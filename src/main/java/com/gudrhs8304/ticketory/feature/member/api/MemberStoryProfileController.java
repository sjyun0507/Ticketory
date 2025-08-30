package com.gudrhs8304.ticketory.feature.member.api;

import com.gudrhs8304.ticketory.feature.member.dto.MemberStoryProfileRes;
import com.gudrhs8304.ticketory.feature.member.MemberStoryProfileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberStoryProfileController {

    private final MemberStoryProfileService service;

    @Operation(summary = "스토리 프로필 조회(내 정보)", description = "{ memberId, name, avatarUrl, lastWatchedAt } 반환")
    @GetMapping("/me")
    public MemberStoryProfileRes me(@AuthenticationPrincipal(expression = "memberId") Long memberId) {
        return service.getProfile(memberId);
    }
}
