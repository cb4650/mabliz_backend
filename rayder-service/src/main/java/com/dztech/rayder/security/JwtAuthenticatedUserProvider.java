package com.dztech.rayder.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticatedUserProvider implements AuthenticatedUserProvider {

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            JwtAuthenticatedUser principal = jwtToken.getPrincipal();
            if (principal != null) {
                return principal.getUserId();
            }
        }
        throw new IllegalStateException("No authenticated user context is available");
    }
}
