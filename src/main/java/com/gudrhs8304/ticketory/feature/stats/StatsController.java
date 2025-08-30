package com.gudrhs8304.ticketory.feature.stats;

import com.gudrhs8304.ticketory.feature.stats.dto.DailyRevenueRes;
import com.gudrhs8304.ticketory.feature.stats.dto.SummaryRes;
import com.gudrhs8304.ticketory.feature.stats.dto.TopMovieRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Admin - 통계", description = "매출/순매출/Top-N 통계 API")
@RestController
@RequestMapping("/api/admin/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @Operation(
            summary = "순매출 요약",
            description = """
                    netRevenue = grossRevenue - refundedAmount
                    범위: [from 00:00:00, to 24:00:00) (to+1일 00:00 미만)
                    """
    )
    @GetMapping("/summary")
    public SummaryRes summary(
            @Parameter(description = "YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.getSummary(from, to);
    }

    @Operation(
            summary = "일자별 승인 매출 합계",
            description = "결제 승인 기준 일합계"
    )
    @GetMapping("/revenue/daily")
    public List<DailyRevenueRes> daily(
            @Parameter(description = "YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.getDailyRevenue(from, to);
    }

    @Operation(
            summary = "영화 Top-N (승인 매출 기준)",
            description = "결제 승인 금액 합계 기준 상위 N 영화"
    )
    @GetMapping("/top-movies")
    public List<TopMovieRes> topMovies(
            @Parameter(description = "YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "가져올 개수", example = "5")
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.getTopMovies(from, to, limit);
    }
}