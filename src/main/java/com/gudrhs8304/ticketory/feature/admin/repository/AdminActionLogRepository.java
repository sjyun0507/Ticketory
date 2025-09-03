package com.gudrhs8304.ticketory.feature.admin.repository;

import com.gudrhs8304.ticketory.feature.admin.domain.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {
    Page<AdminActionLog> findByAdminMember_MemberId(Long adminId, Pageable pageable);
    Page<AdminActionLog> findByActionType(String actionType, Pageable pageable);
    Page<AdminActionLog> findByTargetTableAndTargetId(String table, Long targetId, Pageable pageable);
}
