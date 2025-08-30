package com.gudrhs8304.ticketory.feature.point;

import com.gudrhs8304.ticketory.feature.member.enums.PointChangeType;
import com.gudrhs8304.ticketory.feature.point.dto.PointLogDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PointQueryService {

    private final PointLogRepository pointLogRepository;

    public Page<PointLogDTO> getMemberPointLogs(Long memberId,
                                                LocalDate fromDate,
                                                LocalDate toDate,
                                                List<PointChangeType> types,
                                                int page, int size) {

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100));

        // 기간 해석 (기본: 전체)
        LocalDateTime from = (fromDate != null)
                ? fromDate.atStartOfDay()
                : LocalDate.of(1970, 1, 1).atStartOfDay();
        LocalDateTime to = (toDate != null)
                ? toDate.atTime(LocalTime.MAX)
                : LocalDate.of(2999, 12, 31).atTime(LocalTime.MAX);

        // 타입 해석 (기본: 전체)
        if (types != null && !types.isEmpty()) {
            Set<PointChangeType> set = EnumSet.copyOf(types);
            return pointLogRepository
                    .findByMember_MemberIdAndCreatedAtBetweenAndChangeTypeInOrderByCreatedAtDesc(
                            memberId, from, to, set, pageable
                    )
                    .map(PointLogDTO::from);
        }

        // 타입 미지정 → 기간만
        if (fromDate != null || toDate != null) {
            return pointLogRepository
                    .findByMember_MemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                            memberId, from, to, pageable
                    )
                    .map(PointLogDTO::from);
        }

        // 기본: 전체 이력(최신순)
        return pointLogRepository
                .findByMember_MemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(PointLogDTO::from);
    }
}
