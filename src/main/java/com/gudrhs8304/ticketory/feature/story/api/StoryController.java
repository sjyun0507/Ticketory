package com.gudrhs8304.ticketory.feature.story.api;

import com.gudrhs8304.ticketory.core.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.feature.story.Story;
import com.gudrhs8304.ticketory.feature.story.StoryService;
import com.gudrhs8304.ticketory.feature.story.StorySort;
import com.gudrhs8304.ticketory.feature.story.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Stories")
@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @Operation(summary = "스토리 작성")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<StoryFeedItemView> createStory(
            @AuthenticationPrincipal CustomUserPrincipal p,
            @Valid @RequestBody StoryCreateRequest req
    ) {
        if (p == null) return ResponseEntity.status(401).build();
        return ResponseEntity.status(201)
                .body(storyService.createAndFetchAsFeedItem(p.getMemberId(), req));
    }

    @Operation(summary = "스토리 수정")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{storyId}")
    public ResponseEntity<StoryFeedItemView> updateStory(
            @AuthenticationPrincipal CustomUserPrincipal p,
            @PathVariable Long storyId,
            @Valid @RequestBody StoryUpdateRequest req
    ) {
        if (p == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(
                storyService.updateAndFetchAsFeedItem(p.getMemberId(), storyId, req)
        );
    }

    @Operation(summary = "스토리 삭제 (soft-delete)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> deleteStory(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long storyId
    ) {
        if (principal == null) return ResponseEntity.status(401).build();
        storyService.deleteStory(principal.getMemberId(), storyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 스토리 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(value = "/me", produces = "application/json")
    public ResponseEntity<Page<StoryFeedItemView>> myStories(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        if (principal == null) return ResponseEntity.status(401).build();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                storyService.getMyStoriesAsFeedItems(principal.getMemberId(), pageable)
        );
    }

    @Operation(summary = "스토리 피드 조회")
    @GetMapping(produces = "application/json")
    public ResponseEntity<Page<StoryFeedItemView>> getStories(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "RECENT") StorySort sort,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long memberId
    ) {
        Long viewerId = (principal != null) ? principal.getMemberId() : null;
        return ResponseEntity.ok(
                storyService.getStories(page, size, sort, movieId, memberId, viewerId)
        );
    }


}
