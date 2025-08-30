package com.gudrhs8304.ticketory.feature.screening.dto;

import lombok.*;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ScreeningListResponseDTO {
    private List<ScreeningItemDTO> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static ScreeningListResponseDTO of(Page<ScreeningItemDTO> page) {
        return ScreeningListResponseDTO.builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}