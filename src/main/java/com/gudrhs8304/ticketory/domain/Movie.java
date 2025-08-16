package com.gudrhs8304.ticketory.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Table(name = "movie")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE movie SET delete_at = now() WHERE movie_id = ?")
@Where(clause = "delete_at IS NULL")
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

    @Column(name = "delete_at")
    private LocalDate deletedAt;
}