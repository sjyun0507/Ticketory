package com.gudrhs8304.ticketory.core.jwt;

import com.gudrhs8304.ticketory.feature.member.RoleType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:3600000}") // 기본 1시간
    private long expirationMs;


    public String createToken(Long memberId, RoleType role) {
        Date now = new Date();

        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))    // ← setSubject
                .claim("role", role.name())
                .setIssuedAt(now)                        // ← setIssuedAt
                .setExpiration(exp)                      // ← setExpiration
                .signWith(                               // ← 알고리즘 명시
                        Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token);
    }

    public boolean validate(String token) {
        try {
            parseClaims(token); // 파싱되면 유효
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String createTokenWithClaims(Map<String, Object> claims, Duration ttl) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttl.toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(
                        Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public String createTicketToken(Long bookingId, Long memberId, Long screeningId) {
        Map<String, Object> claims = Map.of(
                "typ", "ticket",
                "bid", bookingId,
                "mid", memberId,
                "sid", screeningId
        );
        // 예: 24시간
        return createTokenWithClaims(claims, Duration.ofHours(24));
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    // validateToken 이름 맞춰주기
    public boolean validateToken(String token) {
        return validate(token);
    }

    // sub 값 읽기
    public String getSubject(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    // 특정 클레임 값 읽기
    public <T> T getClaim(String token, String key, Class<T> type) {
        Object value = parseClaims(token).getPayload().get(key);
        if (value == null) return null;
        return type.cast(value);
    }
}