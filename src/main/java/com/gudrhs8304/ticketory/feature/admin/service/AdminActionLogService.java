package com.gudrhs8304.ticketory.feature.admin.service;

import com.gudrhs8304.ticketory.feature.admin.domain.AdminActionLog;
import com.gudrhs8304.ticketory.feature.admin.repository.AdminActionLogRepository;
import com.gudrhs8304.ticketory.feature.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminActionLogService {
    private final AdminActionLogRepository repo;

    public void log(Long adminId, String actionType, String targetTable, Long targetId,
                    String payloadJson, String ip, String userAgent) {
        AdminActionLog log = AdminActionLog.builder()
                .adminMember(new Member(adminId)) // FK만 세팅
                .actionType(actionType)
                .targetTable(targetTable)
                .targetId(targetId)
                .payloadJson(payloadJson)
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();
        repo.save(log);
    }
}
