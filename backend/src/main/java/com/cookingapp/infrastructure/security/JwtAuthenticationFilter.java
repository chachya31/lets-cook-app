package com.cookingapp.infrastructure.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT認証フィルター
 * リクエストヘッダーからJWTトークンを抽出し、Cognitoで検証してSpring Securityの認証情報を設定する
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final CognitoAuthService cognitoAuthService;

    public JwtAuthenticationFilter(CognitoAuthService cognitoAuthService) {
        this.cognitoAuthService = cognitoAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null) {
                // Cognitoでトークンを検証
                String userId = cognitoAuthService.validateToken(token);

                // JWTからグループ（ロール）を抽出
                List<String> groups = extractGroupsFromToken(token);

                // Spring Securityの権限に変換
                List<GrantedAuthority> authorities = groups.stream()
                        .map(group -> new SimpleGrantedAuthority("ROLE_" + group.toUpperCase()))
                        .collect(Collectors.toList());

                // 認証情報を作成
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,
                        null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContextに設定
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("認証成功: userId={}, groups={}", userId, groups);
            }

        } catch (Exception e) {
            log.error("JWT認証エラー: {}", e.getMessage());
            // 認証失敗時はSecurityContextをクリア
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * リクエストヘッダーからJWTトークンを抽出
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * JWTトークンからCognitoグループを抽出
     */
    private List<String> extractGroupsFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            Claim groupsClaim = jwt.getClaim("cognito:groups");

            if (groupsClaim.isNull()) {
                log.debug("JWTにcognito:groupsクレームが存在しません");
                return new ArrayList<>();
            }

            List<String> groups = groupsClaim.asList(String.class);
            return groups != null ? groups : new ArrayList<>();

        } catch (Exception e) {
            log.error("JWTからグループ抽出エラー: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
