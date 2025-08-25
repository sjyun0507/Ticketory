package com.gudrhs8304.ticketory.config;

import com.gudrhs8304.ticketory.security.JwtAuthFilter;
import com.gudrhs8304.ticketory.security.oauth.CustomOAuth2UserService;
import com.gudrhs8304.ticketory.security.oauth.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${app.security.enabled:true}")                                       // ✅ (1) 프로퍼티 스위치
    private boolean securityEnabled;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtTokenProvider);
    }

    // ⬇️ 카카오 인가요청에 prompt=login 추가
    @Bean
    public OAuth2AuthorizationRequestResolver kakaoAuthRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver delegate =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");
        delegate.setAuthorizationRequestCustomizer(cus ->
                cus.additionalParameters(params -> params.put("prompt", "login"))
        );
        return delegate;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // ✅ (2) 개발용: security 끄면 전부 허용(스웨거/프론트 테스트 편하게)
        if (!securityEnabled) {
            http
                    .csrf(csrf -> csrf.disable())
                    .formLogin(f -> f.disable())
                    .httpBasic(h -> h.disable())
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                    .cors(Customizer.withDefaults())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .oauth2Login(o -> o
                            .authorizationEndpoint(a -> a.authorizationRequestResolver(kakaoAuthRequestResolver()))
                            .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                            .successHandler(oAuth2LoginSuccessHandler)
                    );
            return http.build();
        }

        // ✅ (3) 보안 ON 일 때 기존 설정 유지 + 공개 API만 허용
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // JWT면 stateless 권장
                .cors(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            log.debug("[401] {}", e.getMessage());
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            log.debug("[403] {}", e.getMessage());
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"forbidden\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 정적 리소스 & 파비콘 허용
                        .requestMatchers(
                                "/", "/favicon.ico", "/files/**",
                                "/assets/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/payments-test.html"
                        ).permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()

                        // ✅ (4) 프론트 개발용 공개 GET API (영화 목록/상세, 상영스케줄)
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/screenings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/proxy/**").permitAll()

                        // ===== 공개 엔드포인트 =====
                        .requestMatchers("/login", "/login/success").permitAll()
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/api/members/signup").permitAll()
                        .requestMatchers("/api/members/kakao", "/kakao/logout", "/api/members/logout/kakao").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/members/exists",
                                "/api/members/check-id",
                                "/api/members/check-email",
                                "/api/members/availability"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/members/login",
                                "/api/members/guest-login",
                                "/api/members/logout"
                        ).permitAll()

                        // 관리자 전용
                        .requestMatchers("/api/admin/**", "/login/admin/**").hasRole("ADMIN")

                        // 인증 필요 엔드포인트 (멤버 관련)
                        .requestMatchers(HttpMethod.GET, "/api/members/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/members/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/members/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/*/qr").authenticated()

                        // 그 외
                        .anyRequest().authenticated()
                )
                .oauth2Login(o -> o
                        .authorizationEndpoint(a -> a.authorizationRequestResolver(kakaoAuthRequestResolver()))
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("[OAUTH2-FAIL] {}", exception.getMessage(), exception);
                            try {
                                String code = (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException e)
                                        ? e.getError().getErrorCode() : "unknown";
                                response.sendRedirect("/login?oauth2_error=" + code);
                            } catch (Exception ignored) {
                            }
                        })
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {                        // ✅ (5) 타입 임포트 정리
        return web -> web.ignoring().requestMatchers(
                PathRequest.toStaticResources().atCommonLocations(),
                new AntPathRequestMatcher("/favicon.ico")
        );
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration cfg = new CorsConfiguration();
//        cfg.setAllowedOrigins(List.of("http://localhost:5173")); // 프론트 주소
//        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
//        cfg.setExposedHeaders(List.of("Location", "Content-Disposition"));
//        cfg.setAllowCredentials(true); // fetch에 credentials:true 쓰면 필수
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", cfg);
//        return source;
//    }
}