package com.dztech.auth.security;

import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService tokenService;

    public JwtAuthenticationFilter(JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtTokenService.JwtClaims claims = tokenService.parseAccessToken(token);
            JwtAuthenticatedUser userDetails = new JwtAuthenticatedUser(
                    claims.userId(),
                    claims.username(),
                    claims.email(),
                    claims.phone(),
                    claims.name(),
                    claims.additionalClaims());
            Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, token, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UnsupportedJwtException ex) {
            filterChain.doFilter(request, response);
            return;
        } catch (JwtAuthenticationException ex) {
            SecurityContextHolder.clearContext();
            throw ex;
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(JwtTokenService.JwtClaims claims) {
        Object roleClaim = claims.additionalClaims().get("role");
        if (roleClaim instanceof String role && StringUtils.hasText(role)) {
            String normalized = role.trim().toUpperCase(Locale.ROOT);
            return List.of(new SimpleGrantedAuthority("ROLE_" + normalized));
        }
        return List.of();
    }
}
