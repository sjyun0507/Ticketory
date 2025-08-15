package com.gudrhs8304.ticketory.security;

import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.security.auth.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import java.io.IOException;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;  // 이름도 Provider와 맞춤

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // [CHANGED] Authorization 헤더에서 Bearer 토큰 직접 추출
        String token = resolveToken(request);

        // [CHANGED] JwtTokenProvider.validate 사용 (이 메서드는 이미 존재)
        if (StringUtils.hasText(token) && jwtTokenProvider.validate(token)) {

            // [CHANGED] parseClaims 로 JWS/Claims 가져오기
            Jws<Claims> jws = jwtTokenProvider.parseClaims(token);
            Claims claims = jws.getPayload();

            // [CHANGED] sub(subject) 에 memberId 가 들어있음
            Long memberId = null;
            try { memberId = Long.valueOf(claims.getSubject()); } catch (Exception ignored) {}

            // (선택) QR 티켓 토큰도 허용하려면 typ=ticket 이면 mid 사용
            if (memberId == null && "ticket".equals(claims.get("typ", String.class))) {
                Object mid = claims.get("mid");
                if (mid != null) memberId = Long.valueOf(String.valueOf(mid));
            }

            if (memberId != null) {
                // [CHANGED] "role" 클레임 하나만 사용 → ROLE_ 접두어 붙여 권한 생성
                String role = claims.get("role", String.class); // ex) "USER" or "ADMIN"
                List<SimpleGrantedAuthority> authorities =
                        role == null ? List.of()
                                : List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // [CHANGED] loginId/roles 같은 미존재 클레임 사용 제거
                CustomUserPrincipal principal =
                        new CustomUserPrincipal(memberId, null, null, authorities);

                Authentication auth =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    // [ADDED] Bearer 토큰 추출 유틸
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}