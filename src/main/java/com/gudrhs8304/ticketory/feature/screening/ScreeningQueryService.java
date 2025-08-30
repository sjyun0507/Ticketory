package com.gudrhs8304.ticketory.feature.screening;

import com.gudrhs8304.ticketory.feature.screening.dto.ScreeningDetailResponseDTO;
import com.gudrhs8304.ticketory.feature.screening.dto.ScreeningItemDTO;
import com.gudrhs8304.ticketory.feature.screening.dto.ScreeningListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScreeningQueryService {

    private final ScreeningRepository screeningRepository;

    private static final int MAX_PAGE_SIZE = 200;

    public ScreeningListResponseDTO getScreenings(
            Long movieId,
            LocalDate date,
            Long screenId,      // ✅ theaterId → screenId
            String region,      // ✅ region → Screen.location
            int page,
            int size
    ) {
        LocalDateTime dateStart = (date == null) ? null : date.atStartOfDay();
        LocalDateTime dateEnd   = (date == null) ? null : date.plusDays(1).atStartOfDay();

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.ASC, "startAt").and(Sort.by("screeningId"))
        );

        var pageResult = screeningRepository.search(
                movieId,
                screenId,
                emptyToNull(region),
                dateStart,
                dateEnd,
                pageable
        );

        return ScreeningListResponseDTO.of(pageResult.map(ScreeningItemDTO::from));
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    @Transactional(readOnly = true)
    public ScreeningDetailResponseDTO getScreeningDetail(Long screeningId) {
        Screening s = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영을 찾을 수 없습니다: " + screeningId));

        var movie = s.getMovie();
        var screen = s.getScreen();

        return ScreeningDetailResponseDTO.builder()
                .screeningId(s.getScreeningId())
                .movieId(movie.getMovieId())
                .movieTitle(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .screenId(screen.getScreenId())
                .screenName(screen.getName())
                .location(screen.getLocation())
                .rowCount(screen.getRowCount())
                .colCount(screen.getColCount())
                .startAt(s.getStartAt())
                .endAt(s.getEndAt())
                .basePrice(screen.getBasePrice())   // 💰 여기!
                .build();
    }
}