package com.gudrhs8304.ticketory.config;

import com.gudrhs8304.ticketory.security.JwtAuthFilter;
import com.gudrhs8304.ticketory.security.oauth.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 기본 설정 끄기
                .csrf(csrf -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())

                // 401/403을 리다이렉트 대신 JSON으로
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            log.debug("[401] {}", e.getMessage());
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            log.debug("[403] {}", e.getMessage());
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"forbidden\"}");
                        })
                )

                // 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()

                        // 회원가입/로그인/로그아웃 (백엔드 처리용 엔드포인트는 공개)
                        .requestMatchers(
                                "/api/members",               // POST 회원가입
                                "/api/members/login",         // POST 로그인 (JWT 발급)
                                "/api/members/logout",        // POST 로그아웃 (블랙리스트 방식일 때 토큰만 필요)
                                "/api/members/kakao",         // OAuth2 authorizationEndpoint baseUri
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**",
                                "/login/success", "/login"    // 로그인 결과 페이지들
                        ).permitAll()

                        // 관리자 전용
                        .requestMatchers("/api/admin/**", "/login/admin/**").hasRole("ADMIN")
                        .requestMatchers("api/members/*").authenticated()

                        // 그 외는 인증만 요구
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인(카카오)
                .oauth2Login(o -> o
                        .authorizationEndpoint(ep -> ep.baseUri("/api/members/kakao"))
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .defaultSuccessUrl("/login/success", true)
                        .failureHandler((request, response, exception) -> {
                            log.error("[OAUTH2-FAIL] {}", exception.getMessage(), exception);
                            try {
                                String code = (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException e)
                                        ? e.getError().getErrorCode() : "unknown";
                                response.sendRedirect("/login?oauth2_error=" + code);
                            } catch (Exception ignored) {}
                        })
                )

                // JWT 필터 연결 (UsernamePasswordAuthenticationFilter 앞)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}