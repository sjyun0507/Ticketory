package com.gudrhs8304.ticketory.config;

import com.gudrhs8304.ticketory.security.JwtAuthFilter;
import com.gudrhs8304.ticketory.security.oauth.CustomOAuth2UserService;
import com.gudrhs8304.ticketory.security.oauth.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

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
                cus.additionalParameters(params -> params.put("prompt", "login"))
        );
        return delegate;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            http.csrf(csrf -> csrf.disable())
                    .formLogin(f -> f.disable())
                    .httpBasic(h -> h.disable())
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .cors(Customizer.withDefaults())
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .oauth2Login(o -> o
                            .authorizationEndpoint(e -> e.authorizationRequestResolver(kakaoAuthRequestResolver()))
                            .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                            .successHandler(oAuth2LoginSuccessHandler)
                    );
            return http.build();
        }

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())

                // 401/403을 명확히 반환(404로 가리지 않음)
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

                        // 정적/문서
                        .requestMatchers(
                                "/", "/favicon.ico", "/files/**",
                                "/assets/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/payments-test.html", "/success.html", "/fail.html"
                        ).permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()

                        // 공개 GET
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/screenings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/proxy/**").permitAll()

                        // 인증/소셜
                        .requestMatchers("/login", "/login/success").permitAll()
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()

                        // 멤버 공개 API
                        .requestMatchers("/api/members/signup").permitAll()
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

                        // 결제 플로우
                        .requestMatchers(HttpMethod.POST, "/api/payments/confirm").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/**").authenticated()

                        // 내 정보/탈퇴 (me)
                        .requestMatchers(HttpMethod.GET, "/api/members/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/members/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/members/me").authenticated()

                        // 관리자
                        .requestMatchers("/api/admin/**", "/login/admin/**").hasRole("ADMIN")

                        // 그 외 멤버 API는 인증 필요( /api/members/{id} 포함 )
                        .requestMatchers("/api/members/**").authenticated()

                        // 기타 전부 인증
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
                            } catch (Exception ignored) {}
                        })
                )

                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                PathRequest.toStaticResources().atCommonLocations(),
                new AntPathRequestMatcher("/favicon.ico")
        );
    }
}