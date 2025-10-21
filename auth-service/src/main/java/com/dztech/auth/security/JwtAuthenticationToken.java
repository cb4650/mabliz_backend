package com.dztech.auth.security;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final JwtAuthenticatedUser principal;
    private final String token;

    public JwtAuthenticationToken(
            JwtAuthenticatedUser principal,
            String token,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities == null ? java.util.Collections.emptyList() : authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    public JwtAuthenticationToken(JwtAuthenticatedUser principal, String token) {
        this(principal, token, java.util.Collections.emptyList());
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public JwtAuthenticatedUser getPrincipal() {
        return principal;
    }

    public String getToken() {
        return token;
    }
}
