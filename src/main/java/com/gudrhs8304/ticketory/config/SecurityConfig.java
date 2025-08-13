package com.gudrhs8304.ticketory.config;

import com.gudrhs8304.ticketory.security.oauth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**",
                                "/api/members", "/api/members/login",
                                "/api/members/kakao",
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**").permitAll()
                        .anyRequest()/*.authenticated()*/.permitAll()
                )
                .oauth2Login(o -> o
                        .authorizationEndpoint(ep -> ep.baseUri("/api/members/kakao"))
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .defaultSuccessUrl("/login/success", true)
                        .failureHandler((request, response, exception) -> {
                            // 상세 원인 로깅
                            log.error("[OAUTH2-FAIL] {}", exception.getMessage(), exception);
                            try {
                                // 에러코드 쿼리파라미터로 넘겨서 화면에서 원인 확인 가능
                                String code = (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException e)
                                        ? e.getError().getErrorCode() : "unknown";
                                response.sendRedirect("/login?oauth2_error=" + code);
                            } catch (Exception ignored) {}
                        })
                );

        return http.build();
    }
}
