package com.gudrhs8304.ticketory.feature.member;

import com.gudrhs8304.ticketory.feature.booking.BookingPayStatus;
import com.gudrhs8304.ticketory.feature.story.Story;
import com.gudrhs8304.ticketory.feature.story.StoryRepository;
import com.gudrhs8304.ticketory.feature.story.dto.BookingSummaryRes;
import com.gudrhs8304.ticketory.feature.member.dto.MemberProfileRes;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import com.gudrhs8304.ticketory.feature.story.dto.EligibleBookingRes;
import com.gudrhs8304.ticketory.feature.story.dto.StoryRes;
import com.gudrhs8304.ticketory.feature.story.dto.StorySimpleRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberStoryService {

    private final MemberRepository memberRepository;
    private final BookingRepository bookingRepository;
    private final StoryRepository storyRepository;

    /** 내 프로필 */
    public MemberProfileRes getMyProfile(Long memberId) {
        Member m = memberRepository.findById(memberId).orElseThrow();
        return new MemberProfileRes(
                m.getMemberId(),
                m.getName(),
                m.getAvatarUrl(),
                m.getLastWatchedAt()
        );
    }

    /** 스토리 작성 가능한 예매 목록 */
    public Page<EligibleBookingRes> getEligibleBookings(Long memberId, Pageable pageable) {
        var page = bookingRepository.findEligibleBookingRows(memberId, LocalDateTime.now(), pageable);
        return page.map(this::toEligibleBookingRes);
    }

    private EligibleBookingRes toEligibleBookingRes(Object[] r) {
        return EligibleBookingRes.builder()
                .bookingId((Long) r[0])
                .movieId((Long) r[1])
                .movieTitle((String) r[2])
                .screeningStartAt((java.time.LocalDateTime) r[3])
                .screeningEndAt((java.time.LocalDateTime) r[4])
                .screenName((String) r[5])
                .paymentStatus((BookingPayStatus) r[6])
                .hasStory((Boolean) r[7])
                .build();
    }

    /** 내 스토리 최근 N개 (또는 페이지네이션) */
    public Page<StorySimpleRes> getMyStories(Long memberId, Pageable pageable) {
        var page = storyRepository.findMyStoryRows(memberId, pageable);
        return page.map(this::toStorySimpleRes);
    }

    private StorySimpleRes toStorySimpleRes(Object[] r) {
        return StorySimpleRes.builder()
                .storyId((Long) r[0])
                .movieId((Long) r[1])
                .movieTitle((String) r[2])
                .posterUrl((String) r[3])
                .rating((java.math.BigDecimal) r[4])
                .content((String) r[5])
                .createdAt((java.time.LocalDateTime) r[6])
                .build();
    }




}