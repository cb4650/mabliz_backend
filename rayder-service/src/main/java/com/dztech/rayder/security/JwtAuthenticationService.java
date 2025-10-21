package com.dztech.rayder.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationService {

    private final JwtTokenService tokenService;

    public JwtAuthenticationService(JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public Authentication authenticate(String rawToken) {
        JwtTokenService.JwtClaims claims = tokenService.parseToken(rawToken);
        JwtAuthenticatedUser principal = new JwtAuthenticatedUser(
                claims.userId(),
                claims.username(),
                claims.email(),
                claims.phone(),
                claims.name(),
                claims.additionalClaims());
        return new JwtAuthenticationToken(principal, rawToken);
    }
}
