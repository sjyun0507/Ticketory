package com.gudrhs8304.ticketory.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "movie")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Movie extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(length = 100, nullable = false)
    private String title;

    @Lob
    private String summary;

    @Column(length = 50)
    private String genre;

    @Column(length = 50)
    private String rating;

    @Column(name = "running_minutes")
    private Integer runningMinutes;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    /** 현재 상영 여부 */
    @Column(nullable = false)
    private Boolean status;

    @Lob
    private String actors; // 필요 시 별도 테이블로 분리

    @Column(length = 100)
    private String director;
}