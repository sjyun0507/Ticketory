package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.*;
import com.gudrhs8304.ticketory.domain.enums.PointChangeType;
import com.gudrhs8304.ticketory.repository.MemberRepository;
import com.gudrhs8304.ticketory.repository.PointLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final PointLogRepository pointLogRepository;

    @Transactional
    public void applyAndLog(Member member,
                            Booking booking,
                            Payment payment,
                            PointChangeType type,
                            int signedAmount,        // 적립:+, 사용:–, 취소: 되돌리는 방향
                            String desc) {
        if (member == null || signedAmount == 0) return;

        int current = member.getPointBalance() == null ? 0 : member.getPointBalance();
        int next = current + signedAmount;
        if (next < 0) next = 0; // 음수 방지(정책에 따라 달리 처리해도 됨)
        member.setPointBalance(next);

        PointLog log = PointLog.builder()
                .member(member)
                .booking(booking)
                .payment(payment)
                .changeType(type)
                .amount(signedAmount)     // 부호 포함
                .balanceAfter(next)
                .description(desc)
                .build();
        pointLogRepository.save(log);

        // memberRepository.save(member); // JPA 변경감지면 생략 가능
    }
}
