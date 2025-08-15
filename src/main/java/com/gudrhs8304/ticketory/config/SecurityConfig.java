package com.gudrhs8304.ticketory.config;

import com.gudrhs8304.ticketory.security.JwtAuthFilter;
import com.gudrhs8304.ticketory.security.oauth.CustomOAuth2UserService;
import com.gudrhs8304.ticketory.security.oauth.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

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
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
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

                        // 정적 리소스 & 파비콘 허용 추가
                        .requestMatchers(
                                "/",
                                "/favicon.ico",
                                "/assets/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()

                        // ===== 공개 엔드포인트 =====
                        .requestMatchers("/login", "/login/success").permitAll()
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/api/members/signup").permitAll()
                        // 카카오 시작/로그아웃(둘 다 허용)
                        .requestMatchers("/api/members/kakao", "/kakao/logout", "/api/members/logout/kakao").permitAll()

                        // 일반 로그인/ 게스트 로그인/ 로그아웃
                        .requestMatchers(HttpMethod.POST, "/api/members/login", "/api/members/guest-login", "/api/members/logout").permitAll()

                        // 관리자 전용
                        .requestMatchers("/api/admin/**", "/login/admin/**").hasRole("ADMIN")


                        // ===== 인증 필요 엔드포인트 =====
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
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                org.springframework.boot.autoconfigure.security.servlet.PathRequest.toStaticResources().atCommonLocations(),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/favicon.ico")
        );
    }
}
