package com.gudrhs8304.ticketory.config;

import com.gudrhs8304.ticketory.domain.enums.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

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
    }