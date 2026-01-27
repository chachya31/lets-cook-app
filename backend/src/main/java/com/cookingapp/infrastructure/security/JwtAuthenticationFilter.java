package com.cookingapp.infrastructure.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            DecodedJWT decodedJWT = JWT.decode(token);

            String userId = decodedJWT.getSubject();
            if (userId == null) {
                log.warn("JWT does not contain 'sub' claim");
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = extractAuthorities(decodedJWT);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: {} with authorities: {}", userId, authorities);

        } catch (JWTDecodeException e) {
            log.warn("Failed to decode JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(DecodedJWT decodedJWT) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        Claim groupsClaim = decodedJWT.getClaim("cognito:groups");

        if (!groupsClaim.isNull() && !groupsClaim.isMissing()) {
            List<String> groups = groupsClaim.asList(String.class);
            if (groups != null) {
                for (String group : groups) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + group.toUpperCase()));
                }
            }
        }

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }
}
