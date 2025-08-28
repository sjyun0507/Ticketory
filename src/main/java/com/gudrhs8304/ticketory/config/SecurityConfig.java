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
import org.springframework.core.annotation.Order;
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
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .oauth2Login(o -> o.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // ← 위 CORS 소스 사용
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
                        // 프리플라이트는 무조건 허용
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
//                        .requestMatchers(HttpMethod.PUT, "/api/admin/screenings/**").permitAll()
                        .requestMatchers(
                                "/api/members/signup",
                                "/api/members/login",
                                "/api/members/guest-login",
                                "/api/members/logout",
                                "/api/members/exists",
                                "/api/movies/**",
                                "/api/screenings/**",
                                "/proxy/**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /* ---------- 2) 웹(페이지) 체인 (OAuth2 로그인 리다이렉트 허용) ---------- */
    @Bean
    @Order(2)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/oauth2/**", "/login/**"
                        ).permitAll()
                        .requestMatchers("/api/**").permitAll()

                        .requestMatchers("/", "/assets/**", "/static/**", "/css/**", "/js/**",
                                "/images/**", "/webjars/**", "/favicon.ico",
                                "/payments-test.html", "/success.html", "/fail.html").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(o -> o
                        .authorizationEndpoint(a -> a.baseUri("/oauth2/authorization"))
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                );

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