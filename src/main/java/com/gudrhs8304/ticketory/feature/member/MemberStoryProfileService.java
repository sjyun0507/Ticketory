package com.gudrhs8304.ticketory.feature.member;

import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import com.gudrhs8304.ticketory.feature.member.dto.MemberStoryProfileRes;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberStoryProfileService {

    private final MemberRepository memberRepository;
    private final BookingRepository bookingRepository;

    public Long resolveMemberIdFromPrincipal(Object principal) {
        // TODO: 실제 구현
        // 예: CustomUserPrincipal p = (CustomUserPrincipal) principal; return p.getMemberId();
        throw new IllegalStateException("Principal → memberId 변환 구현 필요");
    }

    public MemberStoryProfileRes getProfile(Long memberId) {
        Member m = memberRepository.findById(memberId).orElseThrow();

        // member.lastWatchedAt 없으면, Booking에서 계산해 보완
        LocalDate lastWatched = m.getLastWatchedAt();
        if (lastWatched == null) {
            lastWatched = bookingRepository.findLastWatchedAt(memberId);
        }

        return new MemberStoryProfileRes(
                m.getMemberId(),
                m.getName(),
                m.getAvatarUrl(),
                lastWatched
        );
    }
}