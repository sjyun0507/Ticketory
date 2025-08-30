package com.gudrhs8304.ticketory.feature.member;

import com.gudrhs8304.ticketory.core.jwt.JwtTokenProvider;
import com.gudrhs8304.ticketory.feature.member.dto.*;
import com.gudrhs8304.ticketory.core.exception.DuplicateLoginIdException;
import com.gudrhs8304.ticketory.core.util.PhoneUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public boolean isLoginIdAvailable(String loginId) {
        if (loginId == null || loginId.isBlank()) return false;
        return !memberRepository.existsByLoginId(loginId.trim());
    }





    public MemberResponseDTO signUp(MemberSignupRequestDTO req) {

        final String loginId = req.getLoginId().trim().toLowerCase();

        // 이메일: 비어있으면 null, 있으면 소문저/trim
        final String email = StringUtils.hasText(req.getEmail()) ? req.getEmail().trim().toLowerCase() : null;

        if (memberRepository.existsByLoginId(req.getLoginId())) {
            throw new DuplicateLoginIdException("이미 사용 중인 이메일입니다.");
        }

        Member member = Member.builder()
                .loginId(loginId)
                .email(email)
                .name(req.getName())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(PhoneUtil.normalize(req.getPhone())) // DB에는 숫자만 저장
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
                .phone(PhoneUtil.format(saved.getPhone())) // 응답 시 하이픈 추가
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
                    if (existing.getSignupType() == SignupType.KAKAO) {
                        throw new IllegalStateException("해당 이메일은 카카오 가입 계정입니다. 카카오 로그인을 이용하세요.");
                    }
                    if (existing.getPassword() == null ||
                            !passwordEncoder.matches(req.getPassword(), existing.getPassword())) {
                        throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
                    }
                    String token = jwtTokenProvider.createToken(existing.getMemberId(), existing.getRole());
                    return new JwtResponseDTO(token, "Bearer");
                })
                .orElseGet(() -> {

                    Member guest = Member.builder()
                            .loginId(email)
                            .email(email)
                            .password(passwordEncoder.encode(req.getPassword()))
                            .name("Guest")
                            .phone(PhoneUtil.normalize(req.getPhone()))
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
                .phone(PhoneUtil.format(saved.getPhone()))
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
                .phone(PhoneUtil.format(m.getPhone()))
                .role(m.getRole().name())
                .points(m.getPointBalance() == null ? 0 : m.getPointBalance())
                .build();
    }

    @Transactional
    public MemberResponseDTO updateMember(Long targetMemberId,
                                          MemberUpdateRequestDTO req,
                                          Long authMemberId,
                                          boolean isAdmin) {
        if (!isAdmin && !targetMemberId.equals(authMemberId)) {
            throw new SecurityException("본인만 수정할 수 있습니다.");
        }

        Member m = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 이름
        if (req.getName() != null && !req.getName().isBlank()) {
            m.setName(req.getName().trim());
        }
        // 핸드폰
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            m.setPhone(PhoneUtil.normalize(req.getPhone())); // DB 저장용
        }
        // 이메일 (null 이면 무시, "" 이면 null 저장하여 제거, 값 있으면 중복 체크 후 저장)
        if (req.getEmail() != null) {
            String newEmail = req.getEmail().trim().toLowerCase();
            if (newEmail.isBlank()) {
                m.setEmail(null); // 이메일 제거
            } else {
                boolean exists = memberRepository.existsByEmailAndMemberIdNot(newEmail, targetMemberId);
                if (exists) {
                    throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
                }
                m.setEmail(newEmail);
            }
        }

        boolean wantsPwChange = (req.getNewPassword() != null && !req.getNewPassword().isBlank());
        if (wantsPwChange) {
            if (isAdmin && !targetMemberId.equals(authMemberId)) {
                m.setPassword(passwordEncoder.encode(req.getNewPassword()));
            } else {
                if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
                    throw new IllegalArgumentException("현재 비밀번호가 필요합니다.");
                }
                if (m.getPassword() == null || !passwordEncoder.matches(req.getCurrentPassword(), m.getPassword())) {
                    throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
                }
                m.setPassword(passwordEncoder.encode(req.getNewPassword()));
            }
        }

        Member saved = memberRepository.save(m);

        return MemberResponseDTO.builder()
                .memberId(saved.getMemberId())
                .loginId(saved.getLoginId())
                .name(saved.getName())
                .email(saved.getEmail())
                .phone(PhoneUtil.format(saved.getPhone())) // 응답 시 하이픈 추가
                .role(saved.getRole().name())
                .build();
    }


    @Transactional
    public void deleteMember(Long targetMemberId, Long requesterId, boolean isAdmin) {
        Member m = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        // 권한 검증(필요시)
        if (!isAdmin && (requesterId == null || !targetMemberId.equals(requesterId))) {
            throw new AccessDeniedException("본인만 탈퇴할 수 있습니다.");
        }

        String tomb = "deleted_" + m.getMemberId();

        m.setActive(false);
        m.setDeletedAt(LocalDateTime.now());

        // 🔴 NOT NULL 컬럼은 NULL 금지 → placeholder로 대체
        m.setName("탈퇴회원"); // ← name NOT NULL 대비
        m.setEmail(tomb + "@ticketory.local"); // ← UNIQUE 충돌 방지
        m.setLoginId(tomb);                    // ← UNIQUE 충돌 방지

        // 비밀번호 컬럼이 NOT NULL이면 더미 값 세팅 (BCrypt 권장)
        // 예) m.setPassword(passwordEncoder.encode("deleted:" + tomb));
        m.setPassword("{noop}deleted"); // PasswordEncoder 안 쓰면 임시로 이렇게

        // nullable 컬럼은 정리
        m.setPhone(null);              // nullable 아니면 "" 로
        m.setSocialId(null);

        // role / signupType 이 NOT NULL이면 값 유지(또는 최소 권한으로 축소)
        // m.setRole(RoleType.USER);
    }

    public Optional<MemberResponseDTO> findOptionalById(Long memberId) {
        return memberRepository.findById(memberId).map(MemberResponseDTO::from);
    }


}