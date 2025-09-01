package com.gudrhs8304.ticketory.feature.member;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LastWatchedService {

    private final BookingRepository bookingRepository;
    private final MemberRepository memberRepository;

    /** 결제 완료 시점에 호출: 상영이 이미 끝났다면 즉시 갱신 */
    @Transactional
    public void onPaymentPaid(Long bookingId) {
        Booking b = bookingRepository.findWithScreeningAndMemberByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("booking not found: " + bookingId));

        var s = b.getScreening();
        var m = b.getMember();

        if (s != null && s.getEndAt() != null && s.getEndAt().isBefore(LocalDateTime.now())) {
            memberRepository.updateLastWatchedIfNewer(m.getMemberId(), s.getEndAt());
        }
    }

    /** 특정 회원만 재계산 */
    @Transactional
    public void recomputeForMember(Long memberId) {
        memberRepository.recomputeLastWatchedForMember(memberId);
    }

    /** 전체 재계산 */
    @Transactional
    public void recomputeAll() {
        memberRepository.recomputeLastWatchedForAll();
    }
}
