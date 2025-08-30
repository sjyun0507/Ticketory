package com.gudrhs8304.ticketory.feature.member;

import com.gudrhs8304.ticketory.feature.member.dto.MemberProfileRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberQueryService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MemberProfileRes getMyProfile(Long memberId) {
        var m = memberRepository.findById(memberId).orElseThrow();
        return new MemberProfileRes(
                m.getMemberId(),
                m.getName(),
                m.getAvatarUrl(),
                m.getLastWatchedAt()
        );
    }
}
