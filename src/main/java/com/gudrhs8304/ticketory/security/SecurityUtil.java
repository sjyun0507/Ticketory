// src/main/java/com/gudrhs8304/ticketory/security/SecurityUtil.java
package com.gudrhs8304.ticketory.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Long currentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object p = auth.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }
        // 커스텀 Principal을 쓰는 경우: getMemberId()가 있으면 사용
        try {
            return (Long) p.getClass().getMethod("getMemberId").invoke(p);
        } catch (Exception ignored) { }
        return null;
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(ga.getAuthority())) return true;
        }
        return false;
    }
}