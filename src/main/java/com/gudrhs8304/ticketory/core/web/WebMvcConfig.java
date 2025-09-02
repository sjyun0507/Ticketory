package com.gudrhs8304.ticketory.core.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 업로드 베이스 디렉터리 (기본: 프로젝트 루트의 uploads/)
     * application.properties에 app.upload.dir=/absolute/or/relative/path 로 변경 가능
     */
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:/files}")
    private String baseUrl;



    /**
     * 정적 리소스 매핑 (예: 업로드된 이미지)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadRoot.toString() + "/");
    }

    /**
     * 인터셉터 등록 (JWT 인증 검증 등)
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // registry.addInterceptor(jwtAuthInterceptor)
        //         .addPathPatterns("/controller/**") // API 요청에만 적용
        //         .excludePathPatterns("/controller/members/login", "/controller/members/signup");
    }
}