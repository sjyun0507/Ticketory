package com.gudrhs8304.ticketory.feature.pricing.api.admin;

import com.gudrhs8304.ticketory.feature.pricing.domain.PricingRule;
import com.gudrhs8304.ticketory.feature.member.enums.PricingKind;
import com.gudrhs8304.ticketory.feature.pricing.dto.PricingRuleDTO;
import com.gudrhs8304.ticketory.feature.pricing.PricingRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/pricing")
public class AdminPricingController {

    private final PricingRuleRepository repo;

    @Operation(
            summary = "상영관 요금 규칙 목록",
            description = """
            상영관(screen) 단위의 요금 규칙을 조회합니다.
            - 필수: screenId
            - 선택: kind (요금 종류)
            정렬: priority ASC, id ASC
            """,
            responses = @ApiResponse(
                    responseCode = "200", description = "규칙 목록",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PricingRule.class))
                    )
            )
    )
    @GetMapping
    public ResponseEntity<List<PricingRule>> list(
            @RequestParam(required = false) Long screenId,
            @RequestParam(required = false) PricingKind kind
    ) {
        List<PricingRule> rules = repo.findByFilter(screenId, kind);
        return ResponseEntity.ok(rules);
    }

    @Operation(summary = "상영관 요금 규칙 등록/수정")
    @PutMapping
    public ResponseEntity<Void> upsertOne(@RequestBody PricingRuleDTO d) {
        saveOne(d);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상영관 요금 규칙 삭제")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void saveOne(PricingRuleDTO d) {
        PricingRule e = (d.getId() == null)
                ? new PricingRule()
                : repo.findById(d.getId()).orElseGet(PricingRule::new);

        // screenId 필수 검증 (신규 생성 시)
        if (e.getId() == null && d.getScreenId() == null) {
            throw new IllegalArgumentException("screenId is required");
        }
        // 업데이트 시에는 전달이 null이면 기존 값 유지
        Long screenId = (d.getScreenId() != null) ? d.getScreenId() : e.getScreenId();
        e.setScreenId(screenId);

        if (d.getKind() != null)     e.setKind(d.getKind());
        if (d.getOp() != null)       e.setOp(d.getOp());
        if (d.getAmount() != null)   e.setAmount(d.getAmount());
        e.setPriority(d.getPriority() == null ? (e.getPriority()==null?100:e.getPriority()) : d.getPriority());
        e.setValidFrom(d.getValidFrom());
        e.setValidTo(d.getValidTo());
        e.setEnabled(d.getEnabled() == null ? (e.getEnabled()==null?Boolean.TRUE:e.getEnabled()) : d.getEnabled());
        e.setCurrency(d.getCurrency() == null ? (e.getCurrency()==null?"KRW":e.getCurrency()) : d.getCurrency());

        repo.save(e);
    }
}