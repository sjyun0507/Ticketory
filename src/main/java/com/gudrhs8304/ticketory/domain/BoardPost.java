package com.gudrhs8304.ticketory.domain;

import com.gudrhs8304.ticketory.domain.enums.Type;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_post",
        indexes = {
                @Index(name="idx_board_type_pub_time", columnList = "type,published,createdAt"),
                @Index(name="idx_board_time", columnList = "createdAt")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardPost {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private Type type;             // EVENT | NOTICE

    @Column(nullable=false, length=200)
    private String title;

    @Lob
    @Column(nullable=false)
    private String content;        // TEXT

    private String bannerUrl;      // nullable

    private LocalDate startDate;   // nullable
    private LocalDate endDate;     // nullable

    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean published;


    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "publish_at")
    private LocalDateTime publishAt;

    @PrePersist
    void prePersist() {
        // 예약발행이면 false, 즉시발행이면 true
        if (published == null) {
            if (publishAt != null && publishAt.isAfter(java.time.LocalDateTime.now())) {
                published = false;  // 예약
            } else {
                published = true;   // 즉시
            }
        }
    }
}
