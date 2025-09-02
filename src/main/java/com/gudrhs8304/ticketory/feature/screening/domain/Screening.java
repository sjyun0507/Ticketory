package com.gudrhs8304.ticketory.feature.screening.domain;

import com.gudrhs8304.ticketory.core.BaseTimeEntity;
import com.gudrhs8304.ticketory.feature.movie.domain.Movie;
import com.gudrhs8304.ticketory.feature.screen.domain.Screen;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "screening")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Screening extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screening_id")
    private Long screeningId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "is_booking", nullable = false)
    private Boolean isBooking = true;
}