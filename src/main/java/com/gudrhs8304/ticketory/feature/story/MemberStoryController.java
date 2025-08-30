package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.story.dto.BookingSummaryRes;
import com.gudrhs8304.ticketory.feature.story.dto.MemberProfileRes;
import com.gudrhs8304.ticketory.feature.story.dto.StoryRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Story/Member")
public class MemberStoryController {

    private final MemberStoryService service;


    @Operation(summary = "스토리 프로필", description = "{ memberId, name, avatarUrl, lastWatchedAt }")
    @GetMapping("/members/me")
    public MemberProfileRes myProfile(@RequestParam("memberId") Long memberId) {
        return service.getMyProfile(memberId);
    }

    @Operation(summary = "스토리 작성가능 예매", description = "상영 종료 & 결제 취소 아님(PAID && cancelled_at IS NULL)")
    @GetMapping("/members/{memberId}/eligible-bookings")
    public Page<BookingSummaryRes> eligibleBookings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.getEligibleBookings(memberId, pageable);
    }

    @Operation(summary = "내 스토리(최근 작성)", description = "최근 N개 페이징")
    @GetMapping("/members/{memberId}/stories")
    public Page<StoryRes> myStories(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.getMyStories(memberId, pageable);
    }
}
