package com.gudrhs8304.ticketory.mail;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mail_log")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MailLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mailLogId;

    private Long bookingId; // 알림메일이면 매핑, 없으면 null

    @Column(nullable = false, length = 255)
    private String recipientEmail;

    @Column(nullable = false, length = 255)
    private String subject;

    @Lob
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Lob
    private String errorMessage;

    @Column(columnDefinition = "datetime(6)")
    private LocalDateTime createdAt;

    public enum Status { SUCCESS, FAIL }
}
