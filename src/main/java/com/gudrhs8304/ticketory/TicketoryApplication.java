package com.gudrhs8304.ticketory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing   // ✅ Auditing 기능 활성화
@SpringBootApplication
public class TicketoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketoryApplication.class, args);
    }

}
