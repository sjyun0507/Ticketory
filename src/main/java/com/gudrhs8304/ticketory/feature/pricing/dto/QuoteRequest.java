package com.gudrhs8304.ticketory.feature.pricing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class QuoteRequest {

    /**
     * 방문일(옵션): 없으면 서버가 Asia/Seoul 현재 날짜 사용
     * 예: 2025-08-27
     */
    private LocalDate visitDate;

    /**
     * 견적 대상 라인아이템
     */
    @NotEmpty
    @Valid
    private List<QuoteItemReq> breakdown;
}
