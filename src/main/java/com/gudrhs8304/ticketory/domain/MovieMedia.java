package com.gudrhs8304.ticketory.domain;

import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movie_media",
        uniqueConstraints = @UniqueConstraint(name = "uk_movie_media_url", columnNames = {"movie_id","url"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieMedia extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MovieMediaType movieMediaType;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(length = 255)
    private String description;
}