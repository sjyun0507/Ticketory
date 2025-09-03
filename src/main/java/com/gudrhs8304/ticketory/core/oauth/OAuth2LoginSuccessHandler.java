package com.gudrhs8304.ticketory.core.oauth;

import com.gudrhs8304.ticketory.core.jwt.JwtTokenProvider;
import com.gudrhs8304.ticketory.feature.member.domain.Member;
import com.gudrhs8304.ticketory.feature.member.enums.RoleType;
import com.gudrhs8304.ticketory.feature.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Log4j2
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Value("${app.frontend.origin:http://localhost:5173}")
    private String frontendOrigin;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2UserService에서 넣어둔 memberId를 우선 사용
        Long memberId = (Long) oAuth2User.getAttributes().get("memberId");
        if (memberId == null) {
            // (선택) 이메일 fallback
            String email = null;
            try {
                var kakaoAccount = (java.util.Map<?, ?>) oAuth2User.getAttributes().get("kakao_account");
                if (kakaoAccount != null) email = (String) kakaoAccount.get("email");
            } catch (Exception ignore) {
            }
            if (email != null) {
                Member m = memberRepository.findByEmail(email).orElseThrow();
                memberId = m.getMemberId();
            } else {
                log.error("OAuth2 success but cannot resolve memberId/email: {}", oAuth2User.getAttributes());
                response.sendRedirect(frontendOrigin + "/login?oauth2_error=no_email");
                return;
            }
        }

        // JWT 발급
        RoleType role = RoleType.USER;
        String jwt = jwtTokenProvider.createToken(memberId, role);

        // 프론트 오리진(5173)으로 토큰을 붙여 리다이렉트 (hash fragment 권장)
        // 해시(#)는 서버 로그/프록시에 덜 남고, 브라우저만 읽습니다.
        String redirect = UriComponentsBuilder
                .fromHttpUrl(frontendOrigin)
                .path("/kakao/")
                .queryParam("token", jwt)
                .build()
                .toUriString();

        response.sendRedirect(redirect);
    }
}