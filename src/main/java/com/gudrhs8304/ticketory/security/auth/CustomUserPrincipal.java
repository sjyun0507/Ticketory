package com.gudrhs8304.ticketory.security.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final Long memberId;
    private final String loginId;
    private final String name; // 필요 없으면 null로 넣어도 됨
    private final Collection<? extends GrantedAuthority> authorities;
}