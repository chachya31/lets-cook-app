package com.cookingapp.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String TEST_USER_ID = "cognito-sub-12345";

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("doFilterInternal")
    class DoFilterInternal {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効なBearerトークンがある場合、SecurityContextに認証情報がセットされる")
            void shouldSetAuthenticationWhenValidBearerTokenProvided() throws ServletException, IOException {
                // Arrange
                String validToken = JWT.create()
                        .withSubject(TEST_USER_ID)
                        .sign(Algorithm.none());

                when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

                // Act
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Assert
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNotNull();
                assertThat(authentication.getPrincipal()).isEqualTo(TEST_USER_ID);
                assertThat(authentication.isAuthenticated()).isTrue();

                verify(filterChain).doFilter(request, response);
            }

            @Test
            @DisplayName("cognito:groups claimがある場合、対応するROLE権限がセットされる")
            void shouldSetRoleAuthoritiesFromCognitoGroups() throws ServletException, IOException {
                // Arrange
                String validToken = JWT.create()
                        .withSubject(TEST_USER_ID)
                        .withClaim("cognito:groups", Arrays.asList("admin", "premium"))
                        .sign(Algorithm.none());

                when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

                // Act
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Assert
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNotNull();

                @SuppressWarnings("unchecked")
                Collection<SimpleGrantedAuthority> authorities =
                        (Collection<SimpleGrantedAuthority>) authentication.getAuthorities();

                assertThat(authorities)
                        .extracting(SimpleGrantedAuthority::getAuthority)
                        .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_PREMIUM");

                verify(filterChain).doFilter(request, response);
            }

            @Test
            @DisplayName("cognito:groups claimがない場合、デフォルトのROLE_USERがセットされる")
            void shouldSetDefaultRoleUserWhenNoGroups() throws ServletException, IOException {
                // Arrange
                String validToken = JWT.create()
                        .withSubject(TEST_USER_ID)
                        .sign(Algorithm.none());

                when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

                // Act
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Assert
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNotNull();

                @SuppressWarnings("unchecked")
                Collection<SimpleGrantedAuthority> authorities =
                        (Collection<SimpleGrantedAuthority>) authentication.getAuthorities();

                assertThat(authorities)
                        .extracting(SimpleGrantedAuthority::getAuthority)
                        .containsExactly("ROLE_USER");

                verify(filterChain).doFilter(request, response);
            }
        }

        @Nested
        @DisplayName("準正常系")
        class SemiNormal {

            @Test
            @DisplayName("Authorizationヘッダーがない場合、認証情報をセットせずに次のフィルターへ進む")
            void shouldProceedWithoutAuthenticationWhenNoAuthorizationHeader() throws ServletException, IOException {
                // Arrange
                when(request.getHeader("Authorization")).thenReturn(null);

                // Act
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Assert
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNull();

                verify(filterChain).doFilter(request, response);
            }

            @Test
            @DisplayName("AuthorizationヘッダーがBearerで始まらない場合、認証情報をセットせずに次のフィルターへ進む")
            void shouldProceedWithoutAuthenticationWhenNotBearerToken() throws ServletException, IOException {
                // Arrange
                when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

                // Act
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Assert
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNull();

                verify(filterChain).doFilter(request, response);
            }

            @Test
            @DisplayName("JWTにsub claimがない場合、認証情報をセットせずに次のフィルターへ進む")
            void shouldProceedWithoutAuthenticationWhenNoSubClaim() throws ServletException, IOException {
                // Arrange
                String tokenWithoutSub = JWT.create()
                        .withClaim("email", "test@example.com")
                        .sign(Algorithm.none());

                when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithoutSub);

                // Act
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Assert
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNull();

                verify(filterChain).doFilter(request, response);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("不正な形式のトークンの場合、認証情報をセットせずに次のフィルターへ進む")
            void shouldProceedWithoutAuthenticationWhenInvalidTokenFormat() throws ServletException, IOException {
                // Arrange
                when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token-format");

                // Act
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Assert
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNull();

                verify(filterChain).doFilter(request, response);
            }

            @Test
            @DisplayName("空のBearerトークンの場合、認証情報をセットせずに次のフィルターへ進む")
            void shouldProceedWithoutAuthenticationWhenEmptyToken() throws ServletException, IOException {
                // Arrange
                when(request.getHeader("Authorization")).thenReturn("Bearer ");

                // Act
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

                // Assert
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNull();

                verify(filterChain).doFilter(request, response);
            }
        }
    }
}
