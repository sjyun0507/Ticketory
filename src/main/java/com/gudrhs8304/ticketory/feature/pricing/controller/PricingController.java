package com.gudrhs8304.ticketory.feature.pricing.controller;

import com.gudrhs8304.ticketory.feature.pricing.dto.QuoteRequest;
import com.gudrhs8304.ticketory.feature.pricing.dto.QuoteResponse;
import com.gudrhs8304.ticketory.feature.pricing.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing", description = "요금/할인 견적 API")
public class PricingController {

    private final PricingService pricingService;

    @Operation(
            summary = "수요일 컬쳐데이 견적",
            description = """
                - 요청 아이템의 합계를 계산하고,
                - 방문일(visitDate)이 수요일이면 20% GLOBAL_WED_DISCOUNT 적용,
                - 최종 견적을 반환합니다.
                - visitDate가 없으면 Asia/Seoul 현재 날짜 기준.
                """)
    @PostMapping(value = "/quote", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest req) {
        return pricingService.quote(req);
    }
}
