package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.domain.Member;
import com.gudrhs8304.ticketory.domain.enums.RoleType;
import com.gudrhs8304.ticketory.dto.JwtResponseDTO;
import com.gudrhs8304.ticketory.dto.MemberLoginRequestDTO;
import com.gudrhs8304.ticketory.dto.MemberResponseDTO;
import com.gudrhs8304.ticketory.dto.MemberSignupRequestDTO;
import com.gudrhs8304.ticketory.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.gudrhs8304.ticketory.domain.enums.SignupType;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberServiceImpl implements MemberService{
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public MemberResponseDTO signUp(MemberSignupRequestDTO req) {
        // 중복 체크
        if (memberRepository.existsByLoginId(req.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // Entity 변환
        Member member = Member.builder()
                .loginId(req.getEmail()) // 이메일을 로그인 ID로
                .email(req.getEmail())
                .name(req.getName())
                .nickname(req.getNickname())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(RoleType.USER)
                .signupType(SignupType.LOCAL) // 일반 가입
                .pointBalance(0)
                .build();

        // 저장
        Member saved = memberRepository.save(member);

        // 응답 DTO 변환
        return MemberResponseDTO.builder()
                .memberId(saved.getMemberId())
                .loginId(saved.getLoginId())
                .name(saved.getName())
                .email(saved.getEmail())
                .nickname(saved.getNickname())
                .phone(saved.getPhone())
                .role(saved.getRole().name())
                .build();
    }

    @Override
    public JwtResponseDTO login(MemberLoginRequestDTO req) {
        log.info("[LOGIN] 요청: loginId={}", req.getLoginId());

        // 1) 사용자 조회
        Member m = memberRepository.findByLoginId(req.getLoginId())
                .orElseThrow(() -> {
                    log.warn("[LOGIN] 존재하지 않는 계정: {}", req.getLoginId());
                    return new IllegalArgumentException("존재하지 않는 계정입니다.");
                });

        // 2) 비밀번호 검증 (LOCAL 기준)
        if (m.getPassword() == null || !passwordEncoder.matches(req.getPassword(), m.getPassword())) {
            log.warn("[LOGIN] 비밀번호 불일치: loginId={}", req.getLoginId());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3) JWT 발급
        String token = jwtTokenProvider.createToken(m.getMemberId(), m.getRole());
        log.info("[LOGIN] JWT 발급 성공: memberId={}", m.getMemberId());

        return new JwtResponseDTO(token, "Bearer");
    }

}
