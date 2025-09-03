package com.gudrhs8304.ticketory.security.handler;

import com.gudrhs8304.ticketory.core.jwt.JwtTokenProvider;
import com.gudrhs8304.ticketory.feature.member.domain.Member;
import com.gudrhs8304.ticketory.feature.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoSuccessHandler implements AuthenticationSuccessHandler {
    private final MemberRepository memberRepository;

    private final JwtTokenProvider jwtTokenProvider; // email/subject로 JWT 만드는 컴포넌트
    @Value("${app.frontend.callback}") String feCallback; // ex) http://localhost:3000/auth/callback

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
        String email = (String) ((Map<?,?>)oAuth2User.getAttributes()
                .get("kakao_account")).get("email");
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        String jwt = jwtTokenProvider.createToken(member.getMemberId(), member.getRole());

        // (A) 프론트로 리다이렉트하며 토큰 전달 (URL fragment 권장)
        String redirect = feCallback + "#token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
        response.sendRedirect(redirect);

        // (B) JSON으로 응답하려면 위 sendRedirect 대신:
        // response.setContentType("application/json");
        // response.getWriter().write("{\"token\":\"" + jwt + "\"}");
    }
}