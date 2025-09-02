// src/main/java/com/gudrhs8304/ticketory/config/SecurityConfig.java
package com.gudrhs8304.ticketory.core.security.config;

import com.gudrhs8304.ticketory.core.jwt.JwtTokenProvider;
import com.gudrhs8304.ticketory.core.jwt.JwtAuthFilter;
import com.gudrhs8304.ticketory.core.oauth.CustomOAuth2UserService;
import com.gudrhs8304.ticketory.core.oauth.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtTokenProvider);
    }


    @Bean
    public OAuth2AuthorizationRequestResolver kakaoAuthRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver delegate =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");
        delegate.setAuthorizationRequestCustomizer(cus ->
                cus.additionalParameters(params -> params.put("prompt", "login")));
        return delegate;
    }

    /** 1) API 전용 체인 (/api/** 만 처리) */
    // API 체인
    @Bean
    @Order(0)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .oauth2Login(o -> o.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .requestCache(rc -> rc.disable()) // ← 추가: 세이브드 리퀘스트 끔
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"forbidden\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 API
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/movies/**",
                                "/api/screenings/**",
                                "/proxy/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**"
                        ).permitAll()

                        // 멤버: 인증 불필요한 엔드포인트만 공개
                        .requestMatchers(
                                "/api/members/signup",
                                "/api/members/login",
                                "/api/members/guest-login",
                                "/api/members/exists"
                        ).permitAll()
                        // 그 외 멤버 관련은 인증 필요
                        .requestMatchers(
                                "/api/members/me",
                                "/api/members/logout",
                                "/api/members/**"
                        ).authenticated()

                        // 스토리: 피드/상세/댓글 조회는 공개, 나머진 인증
                        .requestMatchers(HttpMethod.GET,
                                "/api/stories", "/api/stories/",
                                "/api/stories/*",
                                "/api/stories/*/comments"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/stories/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/api/stories/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/stories/**").authenticated()
                        // 좋아요/북마크 등 액션성 엔드포인트 보호(실제 경로에 맞춰 추가/수정)
                        .requestMatchers(
                                "/api/stories/*/like",
                                "/api/stories/*/unlike",
                                "/api/stories/*/bookmark",
                                "/api/stories/*/unbookmark",
                                "/api/stories/*/comments/**"
                        ).authenticated()

                        // 결제 (현 상태 유지: 필요 시 인증 전환)
                        .requestMatchers(HttpMethod.POST, "/api/payments/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 2) 웹(페이지) 체인 — 절대 /api/** 를 다루지 않음 */
    // 웹 체인
    @Bean
    @Order(1)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**")))
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .requestCache(rc -> rc.disable()) // ← 추가
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 세이프티넷: 혹시 웹 체인이 /api/** 를 잡아도 무조건 통과
                        .requestMatchers("/api/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**",
                                "/swagger-resources/**", "/swagger-ui.html",
                                "/oauth2/**", "/login/**"
                        ).permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(
                                "/", "/assets/**", "/static/**", "/css/**", "/js/**",
                                "/images/**", "/webjars/**", "/favicon.ico",
                                "/payments-test.html", "/success.html", "/fail.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(o -> o
                        .authorizationEndpoint(a -> a.baseUri("/oauth2/authorization"))
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        return http.build();
    }

//    @Bean
//    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
//        return web -> web.ignoring().requestMatchers(
//                PathRequest.toStaticResources().atCommonLocations(),
//                new AntPathRequestMatcher("/favicon.ico")
//        );
//    }
}