package com.gudrhs8304.ticketory.feature.member;

import com.gudrhs8304.ticketory.feature.member.dto.MemberProfileRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;

    public MemberProfileRes getMyProfile(Long memberId) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. id=" + memberId));

        return MemberProfileRes.builder()
                .memberId(m.getMemberId())
                .name(m.getName())
                .avatarUrl(m.getAvatarUrl())
                .lastWatchedAt(m.getLastWatchedAt())
                .build();
    }
}
