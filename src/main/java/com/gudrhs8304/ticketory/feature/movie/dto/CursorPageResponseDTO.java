package com.gudrhs8304.ticketory.feature.movie.dto;

import java.util.List;

public record CursorPageResponseDTO<T>(
        List<T> items,
        String nextCursor,  // 다음 요청에 보낼 커서(없으면 null)
        boolean hasMore
) {}
