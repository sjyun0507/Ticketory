package com.gudrhs8304.ticketory.batch;

import com.gudrhs8304.ticketory.feature.movie.domain.Movie;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "screen_program_slot")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ScreenProgramSlot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long slotId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private ScreenProgramTemplate template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;      // 하루 내 시작 시각

    @Column(length = 7, nullable = false)
    private String weekdays = "YYYYYYY"; // 일월화수목금토 → Y/N

    @Column(nullable = false)
    private Integer priority = 0;     // 같은 시각일 때 정렬용

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt; }

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
