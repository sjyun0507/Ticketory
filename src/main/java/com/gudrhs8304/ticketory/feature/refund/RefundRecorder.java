package com.gudrhs8304.ticketory.feature.refund;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefundRecorder {

    private final RefundLogRepository repo;

    /**
     * 환불 로그 기록
     */
    @Transactional
    public RefundLog record(Long paymentId,
                            Integer refundAmount,
                            String reason,
                            Long processedByAdminId,
                            String pgRefundTid,
                            RefundStatus status // DONE 또는 FAILED
    ) {
        RefundLog log = RefundLog.builder()
                .paymentId(paymentId)
                .refundAmount(refundAmount)
                .reason(reason)
                .processedByAdminId(processedByAdminId)
                .pgRefundTid(pgRefundTid)
                .status(status != null ? status : RefundStatus.DONE)
                .build();
        return repo.save(log);
    }

    @Transactional
    public void markDone(Long refundLogId, String pgRefundTid) {
        RefundLog log = repo.findById(refundLogId).orElseThrow();
        log.setStatus(RefundStatus.DONE);
        if (pgRefundTid != null) log.setPgRefundTid(pgRefundTid);
    }

    @Transactional
    public void markFailed(Long refundLogId, String reason) {
        RefundLog log = repo.findById(refundLogId).orElseThrow();
        log.setStatus(RefundStatus.FAILED);
        if (reason != null) log.setReason(reason);
    }
}
