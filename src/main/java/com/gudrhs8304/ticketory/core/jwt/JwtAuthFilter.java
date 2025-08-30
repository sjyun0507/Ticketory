package com.gudrhs8304.ticketory.core.jwt;

import com.gudrhs8304.ticketory.core.auth.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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

        log.info("[JwtAuthFilter] path={}", ((HttpServletRequest)request).getRequestURI());
        String token = jwtTokenProvider.resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 1) memberId: Number로 받고 longValue()
                Number memberNum = jwtTokenProvider.getClaim(token, "memberId", Number.class);
                Long memberId;
                if (memberNum != null) {
                    memberId = memberNum.longValue();
                } else {
                    // subject fallback (subject가 email일 수 있음 -> 예외)
                    String sub = jwtTokenProvider.getSubject(token);
                    try {
                        memberId = Long.valueOf(sub);
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException("JWT에 memberId 클레임이 없고 subject가 숫자가 아닙니다.");
                    }
                }

                // 2) ROLE 접두사 정규화
                String roleClaim = jwtTokenProvider.getClaim(token, "role", String.class); // "ADMIN" or "ROLE_ADMIN"
                String granted = (roleClaim == null || roleClaim.isBlank())
                        ? "ROLE_USER"
                        : (roleClaim.startsWith("ROLE_") ? roleClaim : "ROLE_" + roleClaim);

                var authorities = List.of(new SimpleGrantedAuthority(granted));

                // 3) principal.username을 memberId 문자열로 (getName() 사용 코드 호환)
                CustomUserPrincipal principal = new CustomUserPrincipal(
                        memberId,
                        String.valueOf(memberId),
                        granted,
                        authorities
                );

                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("AUTH memberId={}, authorities={}", principal.getMemberId(),
                        authorities.stream().map(GrantedAuthority::getAuthority).toList());

            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                log.warn("JWT authentication failed: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }


}
