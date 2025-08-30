package com.gudrhs8304.ticketory.feature.screen.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ScreenDTO {
    // 좌석 한 칸 정의
    public record SeatCell(
            @NotBlank String rowLabel,   // 예: "A"
            @Min(1) int colNumber,       // 예: 1
            @NotBlank String seatType    // "NORMAL" or "VIP"
    ) {}

    // 생성 요청: 둘 중 하나 방식 택1
    // 1) grid 방식을 쓰려면 rows/cols + vipRows 사용
    // 2) 직접좌석 입력은 seats 리스트 사용
    public record CreateRequest(
            @NotNull Long theaterId,
            @NotBlank String name,
            Integer rows,            // grid 방식
            Integer cols,            // grid 방식
            List<String> vipRows,    // grid 방식에서 VIP로 칠할 행 라벨들
            List<SeatCell> seats     // 직접 좌석 배열 (grid 미사용 시)
    ) {}

    public record UpdateRequest(
            String name,                 // 변경할 이름(optional)
            Integer rows, Integer cols,  // grid 재생성용(optional)
            List<String> vipRows,        // optional
            List<SeatCell> seats         // 직접 좌석 재정의(optional)
    ) {}

    public record ScreenResponse(
            Long screenId,
            Long theaterId,
            String name,
            int seatCount
    ) {}
}
