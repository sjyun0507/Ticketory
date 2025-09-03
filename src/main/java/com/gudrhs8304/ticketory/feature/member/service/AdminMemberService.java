package com.gudrhs8304.ticketory.feature.member.service;

import com.gudrhs8304.ticketory.feature.member.domain.Member;
import com.gudrhs8304.ticketory.feature.member.repository.MemberRepository;
import com.gudrhs8304.ticketory.feature.member.enums.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AdminMemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public Member updateMemberRole(Long memberId, RoleType newRole, Long actingAdminId) {
        Member target = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));


        // 유일 ADMIN 보호: 마지막 ADMIN을 USER로 바꾸는 것 방지
        if (target.getRole() == RoleType.ADMIN && newRole == RoleType.USER) {
            long adminCount = memberRepository.countByRole(RoleType.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("최소 1명 이상의 ADMIN이 필요합니다.");
            }
        }

        target.setRole(newRole);
        return memberRepository.save(target);
    }
}
