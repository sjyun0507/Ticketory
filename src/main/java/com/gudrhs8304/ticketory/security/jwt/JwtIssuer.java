package com.gudrhs8304.ticketory.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component
public class JwtIssuer {
//    @Value("${jwt.secret}") private String secret;
//    public String issue(String subject, List<String> roles) {
//        Instant now = Instant.now();
//        return Jwts.builder();
//                .claim(Claims.SUBJECT, subject) // SUBJECT 클레임 직접 지정
//                .setIssuedAt(issuedAt)
//                .setExpiration(expiry)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
}