package com.gudrhs8304.ticketory.feature.member.api;

import com.gudrhs8304.ticketory.core.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.feature.member.GuestLoginRequestDTO;
import com.gudrhs8304.ticketory.feature.member.MemberQueryService;
import com.gudrhs8304.ticketory.feature.member.MemberRepository;
import com.gudrhs8304.ticketory.feature.member.MemberService;
import com.gudrhs8304.ticketory.feature.screening.dto.AvailabilityResponse;
import com.gudrhs8304.ticketory.feature.member.dto.*;
import com.gudrhs8304.ticketory.feature.member.dto.MemberProfileRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member")
@CrossOrigin(origins = "http://localhost:5173")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;

    /**
     * 인증 주체에서 memberId를 안전하게 추출한다.
     * - principal 이 CustomUserPrincipal 객체인 경우(getMemberId 또는 getId)
     * - principal 이 문자열(예: Jwt 필터가 memberId를 name에 저장)인 경우 모두 지원
     */
    private Long extractMemberId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        Object principal = auth.getPrincipal();
        // CustomUserPrincipal 지원
        if (principal instanceof CustomUserPrincipal cup) {
            try {
                return (Long) cup.getClass().getMethod("getMemberId").invoke(cup);
            } catch (Exception ignore) {
                try {
                    return (Long) cup.getClass().getMethod("getId").invoke(cup);
                } catch (Exception e) {
                    throw new AccessDeniedException("인증 주체에서 ID를 추출할 수 없습니다.");
                }
            }
        }
        // 문자열 기반
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("인증 주체가 올바르지 않습니다.");
        }
    }

    @Operation(summary = "회원 가입", description = "일반(LOCAL) 회원 가입 처리", security = {})
    @PostMapping("/signup")
    public MemberResponseDTO signup(@Valid @RequestBody MemberSignupRequestDTO req) {
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

    @Operation(summary = "로그아웃(일반)", description = "클라이언트에서 저장한 액세스 토큰을 삭제하세요.", security = {})
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "마이페이지/회원 정보 조회 — 본인 또는 관리자만", security = {})
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponseDTO> getMember(@PathVariable Long memberId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) throw new AccessDeniedException("로그인이 필요합니다.");
        Long me = extractMemberId(auth);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !memberId.equals(me)) throw new AccessDeniedException("본인 또는 관리자만 접근할 수 있습니다.");
        return ResponseEntity.ok(memberService.getMemberById(memberId));
    }


    @Operation(summary = "회원정보 수정(본인 또는 관리자)")
    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponseDTO> updateMember(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberUpdateRequestDTO req,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
        Long authId = extractMemberId(auth);
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 본인 또는 관리자만 허용
        if (!isAdmin && !memberId.equals(authId)) {
            throw new AccessDeniedException("본인 또는 관리자만 수정할 수 있습니다.");
        }

        MemberResponseDTO res = memberService.updateMember(memberId, req, authId, isAdmin);
        return ResponseEntity.ok(res);
    }

    /** 회원탈퇴 — 본인 */
    @Operation(summary = "회원탈퇴(본인)")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMe(@PathVariable Long memberId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
        Long authId = extractMemberId(auth);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !memberId.equals(authId)) {
            throw new AccessDeniedException("본인 또는 관리자만 삭제할 수 있습니다.");
        }

        memberService.deleteMember(memberId, authId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    /** 회원탈퇴 — 관리자 */
    @Operation(summary = "회원탈퇴(관리자)")
    @DeleteMapping("/admin/{memberId}") // 경로 단순화 및 변수명 일치
    public ResponseEntity<Void> deleteByAdmin(@PathVariable("memberId") Long memberId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) throw new AccessDeniedException("관리자만 삭제할 수 있습니다.");

        memberService.deleteMember(memberId, null, true);
        return ResponseEntity.noContent().build();
    }

}