package com.gudrhs8304.ticketory.core.bootstrap;

import com.gudrhs8304.ticketory.feature.member.domain.Member;
import com.gudrhs8304.ticketory.feature.member.enums.RoleType;
import com.gudrhs8304.ticketory.feature.member.enums.SignupType;
import com.gudrhs8304.ticketory.feature.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class AdminBootstrapConfig {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.enabled:false}")
    private boolean enabled;

    @Value("${app.admin.email:}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;



    @Bean
    public ApplicationRunner adminBootstrapRunner() {
        return args -> {
            if (!enabled) return;
            if (email.isBlank() || password.isBlank()) {
                log.warn("[ADMIN-BOOTSTRAP] email/password 빈 값 — 생성을 건너뜁니다.");
                return;
            }

            memberRepository.findByEmail(email).ifPresentOrElse(existing -> {
                if (existing.getRole() != RoleType.ADMIN) {
                    existing.setRole(RoleType.ADMIN);
                    memberRepository.save(existing);
                    log.info("[ADMIN-BOOTSTRAP] 기존 사용자에 ADMIN 권한 부여: {}", email);
                } else {
                    log.info("[ADMIN-BOOTSTRAP] 이미 ADMIN: {}", email);
                }
            }, () -> {
                Member admin = Member.builder()
                        .loginId(email)
                        .email(email)
                        .name("Admin")
                        .password(passwordEncoder.encode(password))
                        .signupType(SignupType.LOCAL)
                        .role(RoleType.ADMIN)
                        .pointBalance(0)
                        .build();
                memberRepository.save(admin);
                log.info("[ADMIN-BOOTSTRAP] ADMIN 계정 생성: {}", email);
            });
        };
    }
}
