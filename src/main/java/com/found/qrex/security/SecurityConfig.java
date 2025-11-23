package com.found.qrex.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.found.qrex.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;

// ★ 1. (추가) CORS 설정을 위한 Import
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays; // Arrays.asList를 사용하기 위해 임포트

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ★ 2. (수정) 비어있는 람다 대신, 아래에 만든 'corsConfigurationSource()' Bean을 사용하도록 변경
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ 인증 예외 경로 설정 (OPTIONS는 이미 허용되어 있음 - 좋습니다)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🚨 [수정] "/api/auth/**"를 통째로 허용하면 안 됩니다.
                        // 구체적인 경로만 허용(permitAll)하고, 나머지는 인증(authenticated) 받아야 합니다.
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/check-id").permitAll()

                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // Swagger UI 등
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // AI 채팅 중계 경로는 누구나 접근 가능 (내부에서 로그인 체크함)
                        .requestMatchers("/api/ai/chat").permitAll()

                        // AI 에이전트가 사용하는 경로는 인증 없이 허용
                        .requestMatchers("/api/posts/**").permitAll()

                        // ⭐️ [추가] 분석 기록 AI용 경로 허용
                        .requestMatchers("/api/analysis/ai/**").permitAll()

                        // 그 외 모든 요청(특히 /api/auth/profile)은 인증 필요
                        .anyRequest().authenticated()
                )

                // ⬇️ OAuth2 로그인 설정 (기존과 동일)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )

                // ✅ JWT 인증 필터 적용 (기존과 동일)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ★ 3. (추가) 글로벌 CORS 설정을 위한 Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // (중요) Controller의 @CrossOrigin(origins = ...)에 있던 주소들
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://172.30.133.96:5173",
                "http://172.30.129.106:5173",
                "http://172.30.69.67:5173/",
                "http://172.30.1.40:5173/",
                "http://172.30.128.96:5173/",
                "http://172.30.133.16:5173/",
                "http://192.168.130.106:5173",
                "https://www.qrex.kro.kr",
                //혹시
                "http://www.qrex.kro.kr",
                "https://qrex.kro.kr",
                "http://qrex.kro.kr"
        ));

        // (중요) 모든 HTTP 메서드 허용
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // (중요) 모든 헤더 허용 (특히 'Authorization' - JWT 토큰)
        config.setAllowedHeaders(Arrays.asList("*"));

        // (중요) 자격 증명(쿠키, JWT 토큰 등) 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 이 설정 적용
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}