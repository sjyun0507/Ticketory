package com.gudrhs8304.ticketory.feature.story.controller;

import com.gudrhs8304.ticketory.core.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.feature.story.service.BookmarkService;
import com.gudrhs8304.ticketory.feature.story.service.StoryInteractionService;
import com.gudrhs8304.ticketory.feature.story.dto.response.BookmarkedStoryItemDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final StoryInteractionService storyInteractionService;

    /** 북마크 생성 */
    @Operation(summary = "스토리 북마크")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/stories/{storyId}/bookmark")
    public ResponseEntity<?> bookmark(@AuthenticationPrincipal CustomUserPrincipal p,
                                      @PathVariable Long storyId) {
        if (p == null) return ResponseEntity.status(401).build();
        var res = storyInteractionService.bookmark(storyId, p.getMemberId());
        return ResponseEntity.ok(res);
    }

    /** 북마크 취소 */
    @Operation(summary = "스토리 북마크 취소")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("stories/{storyId}/bookmark")
    public ResponseEntity<?> unbookmark(@AuthenticationPrincipal CustomUserPrincipal p,
                                        @PathVariable Long storyId) {
        if (p == null) return ResponseEntity.status(401).build();
        var res = storyInteractionService.unbookmark(storyId, p.getMemberId());
        return ResponseEntity.ok(res);
    }

    /** 내 북마크 목록 (요청하신 스펙) */
    @Operation(summary = "북마크 내역 불러오기")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/members/{memberId}/bookmarked-stories")
    public ResponseEntity<Page<BookmarkedStoryItemDTO>> myBookmarks(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "RECENT") String sort
    ) {
        if (principal == null) return ResponseEntity.status(401).build();
        if (!memberId.equals(principal.getMemberId())) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(bookmarkService.getBookmarkedStories(memberId, page, size, sort));
    }
}
