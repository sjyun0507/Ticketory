package com.gudrhs8304.ticketory.feature.admin;

import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import com.gudrhs8304.ticketory.feature.movie.Movie;
import com.gudrhs8304.ticketory.feature.screen.ScreenRepository;
import com.gudrhs8304.ticketory.feature.screen.Screen;
import com.gudrhs8304.ticketory.feature.screening.ScreeningRepository;
import com.gudrhs8304.ticketory.feature.seat.SeatHoldRepository;
import com.gudrhs8304.ticketory.feature.screening.Screening;
import com.gudrhs8304.ticketory.feature.screening.dto.ScreeningAdminListItemDTO;
import com.gudrhs8304.ticketory.feature.screening.dto.ScreeningAdminResponseDTO;
import com.gudrhs8304.ticketory.feature.screening.dto.ScreeningUpsertRequestDTO;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    // 예약/홀드 검증용 (있다면 주입)
    private final BookingRepository bookingRepository;
    private final SeatHoldRepository seatHoldRepository;

    @Transactional(readOnly = true)
    public Page<ScreeningAdminListItemDTO> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startAt"));
        return screeningRepository.findAllWithJoins(pageable).map(ScreeningAdminListItemDTO::from);
    }

    @Transactional
    public ScreeningAdminResponseDTO create(ScreeningUpsertRequestDTO req) {
        validateTime(req.getStartAt(), req.getEndAt());

        Movie movie = movieRepository.findById(req.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("영화가 존재하지 않습니다: " + req.getMovieId()));
        Screen screen = screenRepository.findById(req.getScreenId())
                .orElseThrow(() -> new IllegalArgumentException("상영관이 존재하지 않습니다: " + req.getScreenId()));

        // 겹침검사
        if (screeningRepository.existsOverlap(screen.getScreenId(), req.getStartAt(), req.getEndAt(), null)) {
            throw new IllegalStateException("해당 상영관 시간대에 다른 상영이 이미 존재합니다.");
        }

        Screening s = Screening.builder()
                .movie(movie)
                .screen(screen)
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .build();

        return ScreeningAdminResponseDTO.from(screeningRepository.save(s));
    }

    @Transactional
    public ScreeningAdminResponseDTO update(Long screeningId, ScreeningUpsertRequestDTO req) {
        validateTime(req.getStartAt(), req.getEndAt());

        Screening s = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영이 존재하지 않습니다: " + screeningId));

        // movie/screen 변경 지원
        if (!s.getMovie().getMovieId().equals(req.getMovieId())) {
            s.setMovie(movieRepository.findById(req.getMovieId())
                    .orElseThrow(() -> new IllegalArgumentException("영화가 존재하지 않습니다: " + req.getMovieId())));
        }
        if (!s.getScreen().getScreenId().equals(req.getScreenId())) {
            s.setScreen(screenRepository.findById(req.getScreenId())
                    .orElseThrow(() -> new IllegalArgumentException("상영관이 존재하지 않습니다: " + req.getScreenId())));
        }

        if (screeningRepository.existsOverlap(s.getScreen().getScreenId(), req.getStartAt(), req.getEndAt(), s.getScreeningId())) {
            throw new IllegalStateException("해당 상영관 시간대에 다른 상영이 이미 존재합니다.");
        }

        s.setStartAt(req.getStartAt());
        s.setEndAt(req.getEndAt());

        return ScreeningAdminResponseDTO.from(s);
    }

    @Transactional
    public void delete(Long screeningId) {
        Screening s = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영이 존재하지 않습니다: " + screeningId));

        // ✅ 예약/홀드 검증
        boolean hasBooking = (bookingRepository != null) &&
                bookingRepository.existsByScreening_ScreeningId(screeningId);

        boolean hasActiveHold = (seatHoldRepository != null) &&
                seatHoldRepository.existsByScreening_ScreeningIdAndExpiresAtAfter(screeningId, LocalDateTime.now());

        if (hasBooking || hasActiveHold) {
            throw new IllegalStateException("예약/좌석홀드가 존재하여 삭제할 수 없습니다.");
        }

        screeningRepository.delete(s);
    }

    private void validateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("startAt/endAt은 필수입니다.");
        }
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("endAt은 startAt 이후여야 합니다.");
        }
    }
}
