package com.cookingapp.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cookingapp.infrastructure.security.JwtAuthenticationFilter;

/**
 * Spring Security設定
 * JWT認証とエンドポイント認可を設定
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS有効化（CorsConfigの設定を使用）
                .cors(cors -> cors.configure(http))

                // CSRF無効化（JWTを使用するため）
                .csrf(AbstractHttpConfigurer::disable)

                // セッション管理をステートレスに設定
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 認可設定
                .authorizeHttpRequests(auth -> auth
                        // 認証不要のエンドポイント
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/confirm",
                                "/api/users/resend-code")
                        .permitAll()

                        // レシピ検索・詳細・レビュー一覧は認証不要
                        .requestMatchers(HttpMethod.GET, "/api/recipes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recipes/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recipes/*/reviews").permitAll()

                        // 管理者APIはADMINSロールが必要
                        .requestMatchers("/api/admin/**").hasRole("ADMINS")

                        // その他のエンドポイントは認証が必要
                        .anyRequest().authenticated())

                // JWTフィルターを追加
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
