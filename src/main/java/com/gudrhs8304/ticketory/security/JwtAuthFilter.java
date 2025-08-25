package com.gudrhs8304.ticketory.security;

import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.security.auth.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                String sub = jwtTokenProvider.getSubject(token);
                Long memberId = Long.valueOf(sub);

                // role 클레임이 있으면 활용, 없으면 USER
                String role = jwtTokenProvider.getClaim(token, "role", String.class);
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER")));

                // ✅ 간편 생성자 사용(위에서 기본 ROLE_USER 넣었다면 여기선 4파라미터로 세팅해도 OK)
                CustomUserPrincipal principal = new CustomUserPrincipal(
                        memberId,
                        "jwtUser-" + memberId,
                        null,
                        authorities
                );

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}