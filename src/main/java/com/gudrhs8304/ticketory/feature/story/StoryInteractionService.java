package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import com.gudrhs8304.ticketory.feature.story.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoryInteractionService {

    private final StoryRepository storyRepository;
    private final StoryLikeRepository likeRepository;
    private final StoryBookmarkRepository bookmarkRepository;
    private final StoryCommentRepository commentRepository;
    private final MemberRepository memberRepository;

    private Member refMember(Long memberId) {
        return memberRepository.getReferenceById(memberId);
    }

    private Story getStoryOrThrow(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다."));
    }

    /** 소프트 삭제 */
    @Transactional
    public void softDelete(Long storyId, Long actorId) {
        Story story = getStoryOrThrow(storyId);
        if (!story.getMember().getMemberId().equals(actorId)) {
            throw new SecurityException("작성자만 삭제할 수 있습니다.");
        }
        story.setStatus(StoryStatus.DELETED);
        storyRepository.save(story);
    }

    /** 좋아요 추가 */
    @Transactional
    public LikeRes like(Long storyId, Long memberId) {
        Story story = getStoryOrThrow(storyId);
        Member me = refMember(memberId);
        if (likeRepository.existsByStory_StoryIdAndMember(storyId, me)) {
            // 이미 좋아요 → 멱등 처리
            long c = likeRepository.countByStory_StoryId(storyId);
            story.setLikeCount((int)c);
            return new LikeRes(c, true);
        }
        likeRepository.save(StoryLike.builder().story(story).member(me).build());
        long cnt = likeRepository.countByStory_StoryId(storyId);
        story.setLikeCount((int)cnt);
        return new LikeRes(cnt, true);
    }

    /** 좋아요 취소 */
    @Transactional
    public LikeRes unlike(Long storyId, Long memberId) {
        Story story = getStoryOrThrow(storyId);
        Member me = refMember(memberId);
        likeRepository.findByStory_StoryIdAndMember(storyId, me).ifPresent(likeRepository::delete);
        long cnt = likeRepository.countByStory_StoryId(storyId);
        story.setLikeCount((int)cnt);
        return new LikeRes(cnt, false);
    }

    /** 북마크 추가 */
    @Transactional
    public BookmarkRes bookmark(Long storyId, Long memberId) {
        Story  story = getStoryOrThrow(storyId);
        Member me    = refMember(memberId);

        if (!bookmarkRepository.existsByStory_StoryIdAndMember_MemberId(storyId, memberId)) {
            bookmarkRepository.save(
                    StoryBookmark.builder()
                            .story(story)   // Story 엔티티!
                            .member(me)     // Member 엔티티!
                            .build()
            );
        }
        return new BookmarkRes(true);
    }

    @Transactional
    public BookmarkRes unbookmark(Long storyId, Long memberId) {
        bookmarkRepository.deleteByStory_StoryIdAndMember_MemberId(storyId, memberId);
        return new BookmarkRes(false);
    }

    /** 댓글 목록 */
    @Transactional
    public Page<CommentRes> listComments(Long storyId, Pageable pageable) {
        Page<StoryComment> page = commentRepository.findByStoryIdOrderByCreatedAtAsc(storyId, pageable);
        return page.map(c -> new CommentRes(
                c.getCommentId(),
                new CommentRes.MemberMini(
                        c.getMember().getMemberId(),
                        c.getMember().getName(),
                        c.getMember().getAvatarUrl()
                ),
                c.getContent(),
                c.getCreatedAt(),
                c.getUpdatedAt() != null
        ));
    }

    /** 댓글 추가 */
    @Transactional
    public CommentRes addComment(Long storyId, Long memberId, String content) {
        Story story = getStoryOrThrow(storyId);
        Member me = refMember(memberId);
        StoryComment c = commentRepository.save(
                StoryComment.builder()
                        .storyId(storyId)
                        .member(me)
                        .content(content)
                        .build()
        );
        long cnt = commentRepository.countByStoryId(storyId);
        story.setCommentCount((int)cnt);

        return new CommentRes(
                c.getCommentId(),
                new CommentRes.MemberMini(me.getMemberId(), me.getName(), me.getAvatarUrl()),
                c.getContent(),
                c.getCreatedAt(),
                false
        );
    }

    /** 댓글 수정 (작성자만) */
    @Transactional
    public CommentRes editComment(Long storyId, Long commentId, Long memberId, String content) {
        StoryComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 없습니다."));
        if (!c.getStoryId().equals(storyId)) throw new IllegalArgumentException("스토리/댓글 불일치");
        if (!c.getMember().getMemberId().equals(memberId)) throw new SecurityException("작성자만 수정 가능");

        c.setContent(content); // @PreUpdate로 updatedAt 갱신
        // 저장 후 응답
        return new CommentRes(
                c.getCommentId(),
                new CommentRes.MemberMini(
                        c.getMember().getMemberId(), c.getMember().getName(), c.getMember().getAvatarUrl()
                ),
                c.getContent(),
                c.getCreatedAt(),
                true
        );
    }

    /** 댓글 삭제 (작성자만) */
    @Transactional
    public void deleteComment(Long storyId, Long commentId, Long memberId) {
        StoryComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 없습니다."));
        if (!c.getStoryId().equals(storyId)) throw new IllegalArgumentException("스토리/댓글 불일치");
        if (!c.getMember().getMemberId().equals(memberId)) throw new SecurityException("작성자만 삭제 가능");

        commentRepository.delete(c);

        // 카운트 갱신
        Story story = getStoryOrThrow(storyId);
        long cnt = commentRepository.countByStoryId(storyId);
        story.setCommentCount((int)cnt);
    }
}
