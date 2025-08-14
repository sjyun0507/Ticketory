package com.gudrhs8304.ticketory.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * CORS 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 엔드포인트 허용
                .allowedOrigins("http://localhost:5137", "http://127.0.0.1:5137") // React 개발 서버
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // 쿠키, 인증정보 허용
                .maxAge(3600);
    }

    /**
     * 정적 리소스 매핑 (예: 업로드된 이미지)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // 로컬 uploads 폴더 매핑
    }

    /**
     * 인터셉터 등록 (JWT 인증 검증 등)
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // registry.addInterceptor(jwtAuthInterceptor)
        //         .addPathPatterns("/api/**") // API 요청에만 적용
        //         .excludePathPatterns("/api/members/login", "/api/members/signup");
    }
}