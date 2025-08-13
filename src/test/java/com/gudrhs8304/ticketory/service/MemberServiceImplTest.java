package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.Member;
import com.gudrhs8304.ticketory.domain.enums.RoleType;
import com.gudrhs8304.ticketory.dto.MemberResponseDTO;
import com.gudrhs8304.ticketory.dto.MemberSignupRequestDTO;
import com.gudrhs8304.ticketory.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
class MemberServiceImplTest {


    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public MemberServiceImplTest(MemberService memberService,
                                 MemberRepository memberRepository,
                                 PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private MemberSignupRequestDTO signupReq(String email, String name, String pw, String nickname) {
        MemberSignupRequestDTO req = new MemberSignupRequestDTO();
        log.info("[TEST] signupReq: email={}, name={}, nickname={} ", email, name, nickname);
        req.setEmail(email);
        req.setLoginId(email);
        req.setName(name);
        req.setPassword(pw);
        req.setNickname(nickname);
        return req;
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUp_success() {
        log.info("[TEST] START signUp_success");
        // given
        String email = "test2@example.com";
        String rawPw = "Pass1234!";
        MemberSignupRequestDTO req = signupReq(email, "홍길동", rawPw, "길동이");

        log.info("[TEST] calling memberService.signUp(email={})", email);
        // when
        MemberResponseDTO res = memberService.signUp(req);
        log.info("[TEST] signUp_success response: memberId={}, loginId={}, role={}", res.getMemberId(), res.getLoginId(), res.getRole());

        // then (응답 DTO 검증)
        assertNotNull(res);
        assertNotNull(res.getMemberId());
        assertEquals(email, res.getLoginId());
        assertEquals(RoleType.USER.name(), res.getRole());

        log.info("[TEST] fetching saved member by loginId={}", email);
        // then (DB 저장값 검증)
        Member saved = memberRepository.findByLoginId(email).orElseThrow();
        log.info("[TEST] saved entity: memberId={}, role={}, pointBalance={}, password(len)={}", saved.getMemberId(), saved.getRole(), saved.getPointBalance(), saved.getPassword() == null ? 0 : saved.getPassword().length());
        assertEquals(RoleType.USER, saved.getRole());
        assertEquals(0, saved.getPointBalance());
        log.info("[TEST] verifying password matches rawPw");
        assertTrue(passwordEncoder.matches(rawPw, saved.getPassword()));
        log.info("[TEST] END signUp_success");
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signUp_duplicate() {
        log.info("[TEST] START signUp_duplicate");
        // given
        String email = "dup@example.com";
        log.info("[TEST] preparing first signup with email={}", email);
        memberService.signUp(signupReq(email, "첫사용자", "Pass1234!", "첫"));
        MemberSignupRequestDTO dup = signupReq(email, "둘사용자", "Pass5678!", "둘");

        log.info("[TEST] attempting duplicate signup with email={}", email);
        // when / then
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> memberService.signUp(dup));
        log.info("[TEST] caught expected exception: {}", ex.getMessage());
        assertTrue(ex.getMessage().contains("이미 사용 중인 이메일"));
        log.info("[TEST] END signUp_duplicate");
    }
}