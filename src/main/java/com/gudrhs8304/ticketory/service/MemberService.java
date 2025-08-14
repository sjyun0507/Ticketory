package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.domain.Member;
import com.gudrhs8304.ticketory.domain.enums.RoleType;
import com.gudrhs8304.ticketory.domain.enums.SignupType;
import com.gudrhs8304.ticketory.dto.*;
import com.gudrhs8304.ticketory.exception.DuplicateLoginIdException;
import com.gudrhs8304.ticketory.repository.BookingRepository;
import com.gudrhs8304.ticketory.repository.MemberRepository;
import com.gudrhs8304.ticketory.util.PhoneUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final BookingRepository bookingRepository;

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


        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            m.setPhone(PhoneUtil.normalize(req.getPhone())); // DB 저장용
        }
        if (req.getProfileImageUrl() != null) {
            m.setProfileImageUrl(req.getProfileImageUrl());
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
    public void deleteMember(Long targetMemberId, Long authMemberId, boolean isAdmin) {
        if (!isAdmin && !targetMemberId.equals(authMemberId)) {
            throw new SecurityException("본인만 탈퇴할 수 있습니다.");
        }

        // (선택) 연관 데이터 정리 필요 시 여기서 처리 (예: 예약/좌석 홀드 등)
        // bookingSeatRepository.deleteByMemberId(targetMemberId);
        // bookingRepository.deleteByMemberId(targetMemberId);
        // ... DB 제약조건에 따라 정리

        memberRepository.deleteById(targetMemberId);
    }
}