package com.dztech.auth.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthenticatedUserProvider {

    private static final String ADMIN_ROLE = "ADMIN";

    public Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            JwtAuthenticatedUser principal = jwtAuthenticationToken.getPrincipal();
            if (principal != null && ADMIN_ROLE.equalsIgnoreCase(String.valueOf(principal.getClaims().get("role")))) {
                return principal.getUserId();
            }
        }
        throw new AccessDeniedException("Admin access token required");
    }
}
