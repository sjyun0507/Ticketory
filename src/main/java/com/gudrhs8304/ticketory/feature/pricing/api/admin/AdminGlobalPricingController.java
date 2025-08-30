package com.gudrhs8304.ticketory.feature.pricing.api.admin;

import com.gudrhs8304.ticketory.feature.member.enums.PricingKind;
import com.gudrhs8304.ticketory.feature.pricing.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/pricing/global")
@RequiredArgsConstructor
public class AdminGlobalPricingController {

    private final PricingService service;

    // 예) POST /api/admin/pricing/global/wed-discount?from=2025-09-01&to=2026-02-28&percent=20
    @PostMapping("/wed-discount")
    public ResponseEntity<Map<String, Object>> genWed(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal percent,
            @RequestParam(required = false) List<PricingKind> kinds // 선택: 특정 Kind만
    ) {
        int n = service.upsertGlobalWednesdayDiscount(
                LocalDate.parse(from), LocalDate.parse(to), percent, kinds
        );
        return ResponseEntity.ok(Map.of("created", n));
    }
}
