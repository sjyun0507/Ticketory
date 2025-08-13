package com.gudrhs8304.ticketory.security.oauth;

import com.gudrhs8304.ticketory.domain.Member;
import com.gudrhs8304.ticketory.domain.enums.RoleType;
import com.gudrhs8304.ticketory.domain.enums.SignupType;
import com.gudrhs8304.ticketory.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attrs = oAuth2User.getAttributes();

        // Kakao 필드 파싱
        Long kakaoId = getKakaoId(attrs);
        String email = getKakaoEmail(attrs);
        String nickname = getKakaoNickname(attrs);

        log.info("[KAKAO] id={}, email={}, nickname={}", kakaoId, email, nickname);

        // 닉네임/로그인ID 대체값 보정
        String fallbackId = "kakao_" + (kakaoId != null ? kakaoId : System.currentTimeMillis());
        String safeLoginId = (email != null && !email.isBlank()) ? email : fallbackId;
        String safeNickname = (nickname != null && !nickname.isBlank()) ? nickname : fallbackId;
        String safeName = safeNickname;

        // 이메일이 없으면 바로 신규 생성 분기로
        Optional<Member> found = (email != null && !email.isBlank())
                ? memberRepository.findByEmail(email)
                : Optional.empty();

        Member member = found.orElseGet(() -> {
            Member toSave = Member.builder()
                    .loginId(safeLoginId)
                    .email(email)                 // null 허용
                    .name(safeName)
                    .nickname(safeNickname)       // NOT NULL 보장
                    .role(RoleType.USER)
                    .signupType(SignupType.KAKAO)
                    .socialId(kakaoId != null ? kakaoId.toString() : null)
                    .pointBalance(0)
                    .build();
            Member saved = memberRepository.save(toSave);
            log.info("[KAKAO] 신규 회원 저장 memberId={}, loginId={}", saved.getMemberId(), saved.getLoginId());
            return saved;
        });

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())),
                attrs,
                "id" // Kakao의 name attribute key
        );
    }

    private Long getKakaoId(Map<String, Object> attrs) {
        Object id = attrs.get("id");
        if (id instanceof Number n) return n.longValue();
        return (id != null) ? Long.parseLong(id.toString()) : null;
    }

    private String getKakaoEmail(Map<String, Object> attrs) {
        Object account = attrs.get("kakao_account");
        if (account instanceof Map<?, ?> m) {
            Object email = m.get("email");
            return email != null ? email.toString() : null;
        }
        return null;
    }

    private String getKakaoNickname(Map<String, Object> attrs) {
        Object account = attrs.get("kakao_account");
        if (account instanceof Map<?, ?> m) {
            Object profile = m.get("profile");
            if (profile instanceof Map<?, ?> pm) {
                Object nick = pm.get("nickname");
                if (nick != null) return nick.toString();
            }
        }
        Object properties = attrs.get("properties"); // 예전 스펙 fallback
        if (properties instanceof Map<?, ?> p) {
            Object nick = p.get("nickname");
            if (nick != null) return nick.toString();
        }
        return null;
    }
}