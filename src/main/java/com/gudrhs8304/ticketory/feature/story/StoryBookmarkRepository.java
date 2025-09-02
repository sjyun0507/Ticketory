package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryBookmarkRepository extends JpaRepository<StoryBookmark, Long> {

    // 존재 여부
    boolean existsByStory_StoryIdAndMember_MemberId(Long storyId, Long memberId);

    // 단건 찾기
    Optional<StoryBookmark> findByStory_StoryIdAndMember_MemberId(Long storyId, Long memberId);

    // 삭제에 쓸 수 있음(선택)
    long deleteByStory_StoryIdAndMember_MemberId(Long storyId, Long memberId);


}
