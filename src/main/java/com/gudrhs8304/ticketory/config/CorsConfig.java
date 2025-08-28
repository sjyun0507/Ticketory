package com.gudrhs8304.ticketory.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    @Primary
    public CorsConfigurationSource corsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 크리덴셜(true)일 땐 "*" 금지 → 개발 도메인 명시
        cfg.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://localhost:5173",
                "https://127.0.0.1:5173"
        ));
        // 메서드/헤더 전부 허용 + 프리플라이트 캐시
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        // 필요 시 노출 헤더
        cfg.setExposedHeaders(List.of("Location","Idempotency-Key"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // 전역 CORS 필터를 최우선으로
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsSource) {
        FilterRegistrationBean<CorsFilter> bean =
                new FilterRegistrationBean<>(new CorsFilter(corsSource));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
