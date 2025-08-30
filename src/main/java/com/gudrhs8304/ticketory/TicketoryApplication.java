package com.gudrhs8304.ticketory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.gudrhs8304.ticketory")
@EntityScan(basePackages = "com.gudrhs8304.ticketory")
@EnableJpaRepositories(basePackages = "com.gudrhs8304.ticketory")
public class TicketoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketoryApplication.class, args);
    }

}
