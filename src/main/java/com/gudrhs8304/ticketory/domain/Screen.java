package com.gudrhs8304.ticketory.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "screen")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Screen extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screen_id")
    private Long screenId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "row_count", nullable = false)
    private Integer rowCount;

    @Column(name = "col_count", nullable = false)
    private Integer colCount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(length = 255)
    private String location;

    @Lob
    private String description;
}