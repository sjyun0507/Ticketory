package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.PricingRule;
import com.gudrhs8304.ticketory.domain.enums.PricingKind;
import com.gudrhs8304.ticketory.dto.pricing.PricingRuleDTO;
import com.gudrhs8304.ticketory.repository.PricingRuleRepository;
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
            @RequestParam Long screenId,
            @RequestParam(required = false) PricingKind kind
    ) {
        List<PricingRule> rules =
                (kind == null)
                        ? repo.findByScreenIdAndEnabledTrueOrderByPriorityAscIdAsc(screenId)
                        : repo.findByScreenIdAndKindAndEnabledTrueOrderByPriorityAscIdAsc(screenId, kind);
        return ResponseEntity.ok(rules);
    }

    @Operation(
            summary = "상영관 요금 규칙 등록/수정",
            description = """
            JSON 배열로 전달 시 각 항목을 upsert 합니다.
            - DTO의 screenId를 그대로 저장합니다.
            - id가 null이면 신규, 있으면 해당 id 갱신.
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PricingRuleDTO.class))
                    )
            ),
            responses = @ApiResponse(responseCode = "204", description = "처리됨")
    )
    @PutMapping
    public ResponseEntity<Void> upsert(@RequestBody List<PricingRuleDTO> incoming) {
        for (PricingRuleDTO d : incoming) {
            PricingRule e = (d.getId() == null) ? new PricingRule()
                    : repo.findById(d.getId()).orElseGet(PricingRule::new);

            e.setScreenId(d.getScreenId());
            e.setKind(d.getKind());
            e.setOp(d.getOp());
            e.setAmount(d.getAmount());
            e.setPriority(d.getPriority() == null ? 100 : d.getPriority());
            e.setValidFrom(d.getValidFrom());
            e.setValidTo(d.getValidTo());
            e.setEnabled(d.getEnabled() == null ? Boolean.TRUE : d.getEnabled());

            repo.save(e);
        }
        return ResponseEntity.noContent().build();
    }
}