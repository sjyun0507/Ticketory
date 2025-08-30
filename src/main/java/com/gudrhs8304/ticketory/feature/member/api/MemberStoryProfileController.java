package com.gudrhs8304.ticketory.feature.member.api;

import com.gudrhs8304.ticketory.feature.member.MemberQueryService;
import com.gudrhs8304.ticketory.feature.member.dto.MemberProfileRes;
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

    private final MemberQueryService memberQueryService;

    @Operation(summary = "스토리 프로필(내 정보)", description = "{ memberId, name, avatarUrl, lastWatchedAt }")
    @GetMapping("/me")
    public MemberProfileRes me(@AuthenticationPrincipal(expression = "memberId") Long memberId) {
        return memberQueryService.getMyProfile(memberId);
    }
}
