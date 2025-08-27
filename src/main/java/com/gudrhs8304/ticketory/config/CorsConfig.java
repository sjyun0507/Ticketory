package com.gudrhs8304.ticketory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 개발 프론트 도메인
        cfg.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://localhost:5173",
                "https://127.0.0.1:5173"
        ));
        // 사용하는 메서드
        cfg.addAllowedMethod("*");
        // 허용할 요청 헤더 (여기에 Idempotency-Key 포함!)
        cfg.addAllowedHeader("*");
        // 클라이언트로 노출할 응답 헤더(선택)
        cfg.setExposedHeaders(List.of("Location","Idempotency-Key"));

        // 인증/쿠키를 쓸 경우 true (아니면 생략 가능)
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
