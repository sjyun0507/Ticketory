package com.gudrhs8304.ticketory.core.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final Long memberId;
    private final String loginId;
    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;

    // ✅ JWT 용 간편 생성자 (authorities 기본 ROLE_USER)
    public CustomUserPrincipal(Long memberId) {
        this.memberId = memberId;
        this.loginId = "jwtUser-" + memberId;
        this.name = null;
        this.authorities = List.of(); // 또는 List.of(new SimpleGrantedAuthority("ROLE_USER"))
    }
}