package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.domain.Member;
import com.gudrhs8304.ticketory.domain.enums.RoleType;
import com.gudrhs8304.ticketory.domain.enums.SignupType;
import com.gudrhs8304.ticketory.dto.GuestLoginRequestDTO;
import com.gudrhs8304.ticketory.dto.JwtResponseDTO;
import com.gudrhs8304.ticketory.dto.MemberLoginRequestDTO;
import com.gudrhs8304.ticketory.dto.MemberResponseDTO;
import com.gudrhs8304.ticketory.dto.MemberSignupRequestDTO;
import com.gudrhs8304.ticketory.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberResponseDTO signUp(MemberSignupRequestDTO req) {
        // 중복 체크 (로그인 아이디 = 이메일 정책)
        if (memberRepository.existsByLoginId(req.getLoginId())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .loginId(req.getLoginId())
                .email(req.getEmail())
                .name(req.getName())
                .nickname(req.getNickname())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(RoleType.USER)
                .signupType(SignupType.LOCAL)
                .pointBalance(0)
                .build();

        Member saved = memberRepository.save(member);

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

    public JwtResponseDTO login(MemberLoginRequestDTO req) {
        log.info("[LOGIN] 요청: loginId={}", req.getLoginId());

        Member m = memberRepository.findByLoginId(req.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (m.getPassword() == null || !passwordEncoder.matches(req.getPassword(), m.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(m.getMemberId(), m.getRole());
        return new JwtResponseDTO(token, "Bearer");
    }

    public JwtResponseDTO guestEmailLogin(GuestLoginRequestDTO req) {
        final String email = req.getEmail().trim().toLowerCase();

        return memberRepository.findByEmail(email)
                .map(existing -> {
                    // 1) 소셜(카카오) 계정이면 이메일 탈취 방지
                    if (existing.getSignupType() == SignupType.KAKAO) {
                        throw new IllegalStateException("해당 이메일은 카카오 가입 계정입니다. 카카오 로그인을 이용하세요.");
                    }
                    // 2) LOCAL이면 비밀번호 검증
                    if (existing.getPassword() == null ||
                            !passwordEncoder.matches(req.getPassword(), existing.getPassword())) {
                        throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
                    }
                    // 3) JWT 발급
                    String token = jwtTokenProvider.createToken(existing.getMemberId(), existing.getRole());
                    return new JwtResponseDTO(token, "Bearer");
                })
                .orElseGet(() -> {
                    // 4) 신규 게스트 생성
                    String nickname = (req.getNickname() != null && !req.getNickname().isBlank())
                            ? req.getNickname().trim()
                            : "Guest-" + UUID.randomUUID().toString().substring(0, 8);

                    Member guest = Member.builder()
                            .loginId(email)                 // 로그인 아이디 = 이메일
                            .email(email)
                            .password(passwordEncoder.encode(req.getPassword()))
                            .name("Guest")
                            .nickname(nickname)
                            .phone(req.getPhone())
                            .signupType(SignupType.LOCAL)
                            .role(RoleType.USER)
                            .pointBalance(0)
                            .build();

                    Member saved = memberRepository.save(guest);
                    String token = jwtTokenProvider.createToken(saved.getMemberId(), saved.getRole());
                    return new JwtResponseDTO(token, "Bearer");
                });
    }

    @Transactional
    public MemberResponseDTO updateRole(Long memberId, RoleType newRole) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. id=" + memberId));
        m.setRole(newRole);
        Member saved = memberRepository.save(m);

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

    @Transactional
    public MemberResponseDTO getMemberById(Long memberId) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));

        return MemberResponseDTO.builder()
                .memberId(m.getMemberId())
                .loginId(m.getLoginId())
                .name(m.getName())
                .email(m.getEmail())
                .nickname(m.getNickname())
                .phone(m.getPhone())
                .role(m.getRole().name())
                .build();
    }
}