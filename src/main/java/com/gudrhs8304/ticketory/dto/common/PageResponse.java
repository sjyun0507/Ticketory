package com.gudrhs8304.ticketory.dto.common;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PageResponse<T> {
    List<T> content; // 요청 페이지
    int page;       // 페이지 크기
    int size;
    long totalElements;
    int totalPages;
    boolean last;   // 마지막 페이지 여부
}
