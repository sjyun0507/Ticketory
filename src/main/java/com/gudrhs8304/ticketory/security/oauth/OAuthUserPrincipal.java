package com.gudrhs8304.ticketory.security.oauth;

import com.gudrhs8304.ticketory.domain.enums.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OAuthUserPrincipal implements OAuth2User {

    private final Long memberId;
    private final RoleType role;
    private final Map<String, Object> attrs;

    public OAuthUserPrincipal(Long memberId, RoleType role, Map<String, Object> attrs) {
        this.memberId = memberId;
        this.role = role;
        this.attrs = attrs;
    }

    @Override
    public Map<String, Object> getAttributes() { return attrs; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // name은 unique identifier 용도로 memberId 사용
    @Override
    public String getName() { return String.valueOf(memberId); }

    public Long getMemberId() { return memberId; }
    public RoleType getRole() { return role; }
}
