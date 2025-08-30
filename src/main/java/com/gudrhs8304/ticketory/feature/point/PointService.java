package com.gudrhs8304.ticketory.feature.point;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.payment.Payment;
import com.gudrhs8304.ticketory.feature.member.enums.PointChangeType;
import com.gudrhs8304.ticketory.feature.point.dto.PointLogDTO;
import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gudrhs8304.ticketory.feature.point.domain.PointLog;
import org.springframework.data.domain.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    public Page<PointLogDTO> getLogs(
            Long memberId,
            LocalDate from,       // nullable
            LocalDate to,         // nullable (포함, 서비스에서 +1일 처리)
            List<PointChangeType> types, // nullable/empty 허용
            int page, int size
    ) {
        LocalDateTime fromDt = (from != null) ? from.atStartOfDay() : null;
        // to는 '해당 날짜 끝까지' 포함되도록 +1일의 자정으로
        LocalDateTime toDt   = (to != null) ? to.plusDays(1).atStartOfDay() : null;

        List<PointChangeType> typesParam =
                (types == null || types.isEmpty()) ? null : types;

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt", "id")
        );

        Page<PointLog> logs = pointLogRepository.search(memberId, fromDt, toDt, typesParam, pageable);
        return logs.map(PointLogDTO::from);
    }
}
