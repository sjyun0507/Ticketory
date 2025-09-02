package com.gudrhs8304.ticketory.feature.story.api;

import com.gudrhs8304.ticketory.feature.story.service.StoryInteractionService;
import com.gudrhs8304.ticketory.feature.story.dto.request.CommentCreateReq;
import com.gudrhs8304.ticketory.feature.story.dto.request.CommentUpdateReq;
import com.gudrhs8304.ticketory.feature.story.dto.response.CommentRes;
import com.gudrhs8304.ticketory.feature.story.dto.response.LikeRes;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryInteractionController {

    private final StoryInteractionService service;



    // 좋아요 생성
    @Operation(summary="좋아요 생성")
    @PostMapping("/{storyId}/like")
    public LikeRes like(@PathVariable Long storyId,
                        @AuthenticationPrincipal(expression = "memberId") Long me) {
        return service.like(storyId, me);
    }

    // 좋아요 취소
    @Operation(summary="좋아요 취소")
    @DeleteMapping("/{storyId}/like")
    public LikeRes unlike(@PathVariable Long storyId,
                          @AuthenticationPrincipal(expression = "memberId") Long me) {
        return service.unlike(storyId, me);
    }


    // 댓글 목록
    @Operation(summary="댓글 목록")
    @GetMapping("/{storyId}/comments")
    public Page<CommentRes> listComments(@PathVariable Long storyId,
                                         @RequestParam(defaultValue="0") int page,
                                         @RequestParam(defaultValue="20") int size,
                                         @AuthenticationPrincipal(expression = "memberId") Long me) {
        Pageable pageable = PageRequest.of(page, size);
        return service.listComments(storyId, pageable, me);
    }

    // 댓글 추가
    @Operation(summary="댓글 추가")
    @PostMapping("/{storyId}/comments")
    public CommentRes addComment(@PathVariable Long storyId,
                                 @Valid @RequestBody CommentCreateReq req,
                                 @AuthenticationPrincipal(expression = "memberId") Long me) {
        return service.addComment(storyId, me, req.content());
    }

    // 댓글 수정
    @Operation(summary="댓글 수정")
    @PutMapping("/{storyId}/comments/{commentId}")
    public CommentRes editComment(@PathVariable Long storyId,
                                  @PathVariable Long commentId,
                                  @Valid @RequestBody CommentUpdateReq req,
                                  @AuthenticationPrincipal(expression = "memberId") Long me) {
        return service.editComment(storyId, commentId, me, req.content());
    }

    // 댓글 삭제
    @Operation(summary="댓글 삭제")
    @DeleteMapping("/{storyId}/comments/{commentId}")
    public void deleteComment(@PathVariable Long storyId,
                              @PathVariable Long commentId,
                              @AuthenticationPrincipal(expression = "memberId") Long me) {
        service.deleteComment(storyId, commentId, me);
    }
}
