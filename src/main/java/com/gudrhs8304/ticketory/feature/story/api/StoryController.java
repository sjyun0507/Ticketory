package com.gudrhs8304.ticketory.feature.story.api;

import com.gudrhs8304.ticketory.feature.story.Story;
import com.gudrhs8304.ticketory.feature.story.StoryService;
import com.gudrhs8304.ticketory.feature.story.StorySort;
import com.gudrhs8304.ticketory.feature.story.dto.StoryCreateRequest;
import com.gudrhs8304.ticketory.feature.story.dto.StoryFeedItemDTO;
import com.gudrhs8304.ticketory.feature.story.dto.StoryUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @Operation(summary = "스토리 작성")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Story> createStory(
            @AuthenticationPrincipal(expression = "memberId") Long memberId,
            @Valid @RequestBody StoryCreateRequest req
    ) {
        return ResponseEntity.ok(storyService.createStory(memberId, req));
    }

    @Operation(summary = "스토리 수정")
    @PutMapping("/{storyId}")
    public ResponseEntity<Story> updateStory(
            @AuthenticationPrincipal(expression = "memberId") Long memberId,
            @PathVariable Long storyId,
            @Valid @RequestBody StoryUpdateRequest req
    ) {
        return ResponseEntity.ok(storyService.updateStory(memberId, storyId, req));
    }

    @Operation(summary = "스토리 삭제 (soft-delete)")
    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> deleteStory(
            @AuthenticationPrincipal(expression = "memberId") Long memberId,
            @PathVariable Long storyId
    ) {
        storyService.deleteStory(memberId, storyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 스토리 조회")
    @GetMapping("/me")
    public Page<Story> myStories(
            @AuthenticationPrincipal(expression = "memberId") Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return storyService.getMyStories(memberId, pageable);
    }

    @Operation(summary = "스토리 피드 조회")
    @GetMapping
    public ResponseEntity<Page<StoryFeedItemDTO>> getStories(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "RECENT") StorySort sort,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long memberId
    ) {
        return ResponseEntity.ok(
                storyService.getStories(page, size, sort, movieId, memberId)
        );
    }
}
