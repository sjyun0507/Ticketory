package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.*;
import com.gudrhs8304.ticketory.repository.MemberRepository;
import com.gudrhs8304.ticketory.security.SecurityUtil;
import com.gudrhs8304.ticketory.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member")
@CrossOrigin(origins = "http://localhost:5173")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Operation(summary = "회원 가입", description = "일반(LOCAL) 회원 가입 처리", security = {})
    @PostMapping("/signup")
    @ResponseBody
    public MemberResponseDTO signup(@Valid @RequestBody  MemberSignupRequestDTO req) {
        return memberService.signUp(req);
    }

    @Operation(summary = "로그인", description = "비밀번호 검증 후 JWT 발급", security = {})
    @PostMapping("/login")
    public JwtResponseDTO login(@Valid @RequestBody MemberLoginRequestDTO req) {
        return memberService.login(req);
    }

    @Operation(summary = "아이디 중복 확인")
    @GetMapping(value = "/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public AvailabilityResponse checkLoginId(@RequestParam String loginId) {
        boolean available = memberService.isLoginIdAvailable(loginId);
        return available
                ? new AvailabilityResponse(true, "사용 가능한 아이디입니다.")
                : new AvailabilityResponse(false, "이미 사용 중인 아이디입니다.");
    }

    @Operation(summary = "비회원(게스트) 이메일 로그인", description = """
            - 이메일/비밀번호 입력
            - 존재: LOCAL이면 비번검증→JWT, KAKAO이면 차단
            - 미존재: LOCAL로 게스트 생성→JWT
            """, security = {})
    @PostMapping("/guest-login")
    public JwtResponseDTO guestLogin(@Valid @RequestBody GuestLoginRequestDTO req) {
        return memberService.guestEmailLogin(req);
    }

    @Operation(summary = "로그아웃", security = {})
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "마이페이지/회원 정보 조회 — 본인 또는 관리자만", security = {})
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponseDTO> getMember(@PathVariable Long memberId) {
        Long me = SecurityUtil.currentMemberId();
        if (me == null) throw new AccessDeniedException("로그인이 필요합니다.");
        if (!SecurityUtil.isAdmin() && !memberId.equals(me)) {
            throw new AccessDeniedException("본인 또는 관리자만 접근할 수 있습니다.");
        }
        return ResponseEntity.ok(memberService.getMemberById(memberId));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponseDTO> updateMember(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberUpdateRequestDTO req,
            Authentication authentication
    ) {
        // JwtTokenProvider에서 setSubject(memberId) 했으므로
        // authentication.getName() == "memberId"
        Long authId = Long.valueOf(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        MemberResponseDTO res = memberService.updateMember(memberId, req, authId, isAdmin);
        return ResponseEntity.ok(res);
    }

    // 본인 탈퇴
    @Operation(summary = "회원탈퇴(본인)")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String name = auth.getName(); // JWT 필터가 넣어준 값 (memberId 또는 loginId일 수 있음)
        Long authMemberId = null;

        // 1) name이 숫자면 memberId로 간주
        try {
            authMemberId = Long.valueOf(name);
        } catch (NumberFormatException ignore) { /* 숫자가 아니면 아래로 */ }

        if (authMemberId == null) {
            // 2) 숫자가 아니면 loginId(이메일)로 조회
            authMemberId = memberRepository.findIdByLoginId(name)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        }

        memberService.deleteMember(authMemberId, authMemberId, false);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "회원탈퇴(관리자)")
    @DeleteMapping("/admin/members/{memberId}")
    public ResponseEntity<Void> deleteByAdmin(@PathVariable Long id) {
        // 관리자니까 isAdmin=true
        memberService.deleteMember(id, null, true);
        return ResponseEntity.noContent().build();
    }
}