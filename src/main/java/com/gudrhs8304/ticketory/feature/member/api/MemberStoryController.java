package com.gudrhs8304.ticketory.feature.member.api;

import com.gudrhs8304.ticketory.feature.member.MemberStoryService;
import com.gudrhs8304.ticketory.feature.story.dto.BookingSummaryRes;
import com.gudrhs8304.ticketory.feature.member.dto.MemberProfileRes;
import com.gudrhs8304.ticketory.feature.story.dto.EligibleBookingRes;
import com.gudrhs8304.ticketory.feature.story.dto.StoryRes;
import com.gudrhs8304.ticketory.feature.story.dto.StorySimpleRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Story/Member")
public class MemberStoryController {

    private final MemberStoryService service;



    @Operation(summary = "스토리 작성가능 예매", description = "상영 종료 & 결제 취소 아님 + (hasStory 포함)")
    @GetMapping("/{memberId}/eligible-bookings")
    public Page<EligibleBookingRes> eligibleBookings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.getEligibleBookings(memberId, pageable);
    }

    @Operation(summary = "내 스토리(최근 작성)", description = "배열 또는 페이징")
    @GetMapping("/{memberId}/stories")
    public Page<StorySimpleRes> myStories(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return service.getMyStories(memberId, pageable);
    }
}
