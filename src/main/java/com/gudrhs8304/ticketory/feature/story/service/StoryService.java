package com.gudrhs8304.ticketory.feature.story.service;

import com.gudrhs8304.ticketory.feature.booking.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.feature.booking.repository.BookingRepository;
import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.story.enums.StorySort;
import com.gudrhs8304.ticketory.feature.story.enums.StoryStatus;
import com.gudrhs8304.ticketory.feature.story.domain.Story;
import com.gudrhs8304.ticketory.feature.story.dto.request.StoryCreateRequest;
import com.gudrhs8304.ticketory.feature.story.dto.request.StoryUpdateRequest;
import com.gudrhs8304.ticketory.feature.story.dto.response.StoryFeedItemView;
import com.gudrhs8304.ticketory.feature.story.dto.response.StoryRes;
import com.gudrhs8304.ticketory.feature.story.repository.StoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final BookingRepository bookingRepository;

    /** 스토리 작성: bookingId 필수 */
    @Transactional
    public StoryRes createStory(Long memberId, StoryCreateRequest req) {
        Booking b = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        if (!b.getMember().getMemberId().equals(memberId)) {
            throw new AccessDeniedException("본인 예약만 작성할 수 있습니다.");
        }
        if (b.getPaymentStatus() != BookingPayStatus.PAID) {
            throw new IllegalStateException("결제 완료 예매만 작성할 수 있습니다.");
        }
        if (b.getScreening().getEndAt().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("상영 종료 후에만 작성할 수 있습니다.");
        }

        // 같은 예매로 이미 작성(삭제 제외) 방지
        boolean exists = storyRepository.existsByBooking_BookingIdAndStatusNot(
                b.getBookingId(), StoryStatus.DELETED
        );
        if (exists) throw new IllegalStateException("이미 관람평을 작성한 예매입니다.");

        Story story = storyRepository.save(
                Story.builder()
                        .member(b.getMember())
                        .movie(b.getScreening().getMovie())
                        .booking(b)
                        .content(req.getContent().trim())
                        .rating(req.getRating())
                        .status(StoryStatus.ACTIVE)
                        .build()
        );
        return StoryRes.from(story);
    }

    /** 스토리 수정 (본인만 가능) */
    @Transactional
    public Story updateStory(Long memberId, Long storyId, StoryUpdateRequest req) {
        Story story = storyRepository.findByStoryIdAndStatus(storyId, StoryStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("스토리를 찾을 수 없습니다."));
        if (!story.getMember().getMemberId().equals(memberId)) {
            throw new IllegalStateException("본인 스토리만 수정할 수 있습니다.");
        }
        story.setContent(req.getContent());
        return story; // dirty checking
    }

    /** 스토리 삭제 (soft-delete) */
    @Transactional
    public void deleteStory(Long memberId, Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new EntityNotFoundException("스토리를 찾을 수 없습니다."));
        if (!story.getMember().getMemberId().equals(memberId)) {
            throw new IllegalStateException("본인 스토리만 삭제할 수 있습니다.");
        }
        storyRepository.updateStatus(storyId, StoryStatus.DELETED);
    }

    /** 마이페이지 원본 엔티티 페이지 (기존 용도 유지 시) */
    @Transactional(readOnly = true)
    public Page<Story> getMyStories(Long memberId, Pageable pageable) {
        return storyRepository.findByMember_MemberIdAndStatusOrderByCreatedAtDesc(
                memberId, StoryStatus.ACTIVE, pageable);
    }

    /** 전체 스토리 피드 (DTO 프로젝션) */
    @Transactional(readOnly = true)
    public Page<StoryFeedItemView> getStories(Integer page, Integer size, StorySort sort,
                                              Long movieId, Long memberId, Long viewerId) {
        Pageable pageable = PageRequest.of(
                page, size,
                sort == StorySort.POPULAR
                        ? Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("createdAt"))
                        : Sort.by(Sort.Order.desc("createdAt"))
        );

        if (movieId != null) {
            return storyRepository.findFeedByMovie(movieId, viewerId, pageable);
        }
        if (memberId != null) {
            return storyRepository.findMyFeed(memberId, viewerId, pageable);
        }
        return storyRepository.findFeed(viewerId, pageable);
    }

    /** 생성 직후 피드용 View로 재조회 */
    @Transactional
    public StoryFeedItemView createAndFetchAsFeedItem(Long memberId, StoryCreateRequest req) {
        StoryRes saved = createStory(memberId, req);
        return storyRepository.findOneAsFeedItem(saved.storyId(), memberId)
                .orElseThrow(() -> new IllegalStateException("created story not found"));
    }

    /** 수정 직후 피드용 View로 재조회 */
    @Transactional
    public StoryFeedItemView updateAndFetchAsFeedItem(Long memberId, Long storyId, StoryUpdateRequest req) {
        updateStory(memberId, storyId, req);
        return storyRepository.findOneAsFeedItem(storyId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("story not found: " + storyId));
    }

    /** 마이페이지도 View로 제공 (우측 레일 등에서 사용) */
    @Transactional(readOnly = true)
    public Page<StoryFeedItemView> getMyStoriesAsFeedItems(Long memberId, Pageable pageable) {
        return storyRepository.findMyFeed(memberId, memberId, pageable);
    }
}