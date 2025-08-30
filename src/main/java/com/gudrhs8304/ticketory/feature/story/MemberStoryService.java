package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import com.gudrhs8304.ticketory.feature.story.dto.BookingSummaryRes;
import com.gudrhs8304.ticketory.feature.story.dto.MemberProfileRes;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import com.gudrhs8304.ticketory.feature.story.dto.StoryRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    public Page<BookingSummaryRes> getEligibleBookings(Long memberId, Pageable pageable) {
        Page<Object[]> page = bookingRepository.findEligibleBookingRows(memberId, pageable);
        return page.map(this::mapBookingRow);
    }

    private BookingSummaryRes mapBookingRow(Object[] r) {
        // bookingId, screeningId, movieId, movieTitle, startAt, endAt, paidAmount
        Long bookingId   = ((Number) r[0]).longValue();
        Long screeningId = ((Number) r[1]).longValue();
        Long movieId     = ((Number) r[2]).longValue();
        String title     = (String) r[3];
        LocalDateTime startAt = (r[4] instanceof java.sql.Timestamp ts1) ? ts1.toLocalDateTime() : (LocalDateTime) r[4];
        LocalDateTime endAt   = (r[5] instanceof java.sql.Timestamp ts2) ? ts2.toLocalDateTime() : (LocalDateTime) r[5];
        BigDecimal paidAmount = (r[6] == null) ? BigDecimal.ZERO :
                (r[6] instanceof BigDecimal bd ? bd : new BigDecimal(String.valueOf(r[6])));

        return new BookingSummaryRes(bookingId, screeningId, movieId, title, startAt, endAt, paidAmount);
    }

    /** 내 스토리 최근 N개 */
    public Page<StoryRes> getMyStories(Long memberId, Pageable pageable) {
        Page<Story> page = storyRepository.findByMember_MemberIdOrderByCreatedAtDesc(memberId, pageable);
        return page.map(s -> new StoryRes(
                s.getStoryId(),
                s.getBooking().getBookingId(),
                s.getContent(),
                s.getCreatedAt()
        ));
    }
}