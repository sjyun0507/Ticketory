package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import com.gudrhs8304.ticketory.feature.story.Story;
import com.gudrhs8304.ticketory.feature.story.StoryRepository;
import com.gudrhs8304.ticketory.feature.story.dto.BookmarkedStoryItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final StoryRepository storyRepository;
    private final MemberRepository memberRepository;

    /** 북마크 생성 (멱등) */
    @Transactional
    public void addBookmark(Long memberId, Long storyId) {
        if (bookmarkRepository.existsByMember_MemberIdAndStory_StoryId(memberId, storyId)) return;

        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));
        Story s = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("story not found"));

        bookmarkRepository.save(Bookmark.builder().member(m).story(s).build());
    }

    /** 북마크 삭제 (없어도 OK) */
    @Transactional
    public void removeBookmark(Long memberId, Long storyId) {
        bookmarkRepository.deleteByMemberAndStory(memberId, storyId);
    }

    /** 내 북마크 목록 (viewer = 본인) */
    @Transactional(readOnly = true)
    public Page<BookmarkedStoryItemDTO> getBookmarkedStories(Long memberId, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size,
                "POPULAR".equalsIgnoreCase(sort)
                        ? Sort.by(Sort.Order.desc("s.likeCount"), Sort.Order.desc("s.createdAt"))
                        : Sort.by(Sort.Order.desc( "b.createdAt"))
        );
        return bookmarkRepository.findBookmarkedFeed(memberId, memberId, pageable);
    }
}
