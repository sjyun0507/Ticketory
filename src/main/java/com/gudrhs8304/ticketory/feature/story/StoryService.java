package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import com.gudrhs8304.ticketory.feature.booking.BookingPayStatus;
import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import com.gudrhs8304.ticketory.feature.story.dto.StoryCreateRequest;
import com.gudrhs8304.ticketory.feature.story.dto.StoryUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final MemberRepository memberRepository;
    private final BookingRepository bookingRepository;

    /** 스토리 작성: bookingId 필수 */
    @Transactional
    public Story createStory(Long memberId, StoryCreateRequest req) {
        // 1) 예매 존재
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new EntityNotFoundException("예매를 찾을 수 없습니다."));

        // 2) 소유자 검증
        if (!booking.getMember().getMemberId().equals(memberId)) {
            throw new IllegalStateException("본인 예매에 대해서만 스토리를 작성할 수 있습니다.");
        }

        // 3) 상영 종료 검증
        LocalDateTime endAt = booking.getScreening().getEndAt();
        if (endAt == null || endAt.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("상영 종료 후에만 스토리를 작성할 수 있습니다.");
        }

        // 4) 결제 취소 아님
        if (booking.getPaymentStatus() == BookingPayStatus.CANCELLED) {
            throw new IllegalStateException("결제 취소된 예매는 스토리 작성이 불가합니다.");
        }

        // 5) 중복 작성 방지(활성 스토리 기준)
        boolean exists = storyRepository.existsByMember_MemberIdAndBooking_BookingIdAndStatus(
                memberId, booking.getBookingId(), StoryStatus.ACTIVE);
        if (exists) {
            throw new IllegalStateException("해당 예매로 이미 스토리를 작성했습니다.");
        }

        // 6) 스토리 생성 (movie은 booking → screening → movie에서 얻음)
        Story story = Story.builder()
                .member(memberRepository.getReferenceById(memberId))
                .movie(booking.getScreening().getMovie())
                .booking(booking)
                .content(req.getContent())
                .status(StoryStatus.ACTIVE)
                .build();

        return storyRepository.save(story);
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
        return story; // dirty checking으로 업데이트
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

    /** 내 스토리 목록 조회 */
    @Transactional(readOnly = true)
    public Page<Story> getMyStories(Long memberId, Pageable pageable) {
        return storyRepository.findByMember_MemberIdAndStatusOrderByCreatedAtDesc(
                memberId, StoryStatus.ACTIVE, pageable);
    }
}