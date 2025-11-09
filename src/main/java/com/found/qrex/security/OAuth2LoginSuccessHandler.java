// package com.found.qrex.security;

package com.found.qrex.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final String frontendCallbackUrl;

    public OAuth2LoginSuccessHandler(JwtTokenProvider jwtTokenProvider,
                                     @Value("${app.oauth.frontend-redirect-uri}") String frontendCallbackUrl) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.frontendCallbackUrl = frontendCallbackUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // ⭐️ [핵심 수정]
        // "id" (userId)와 "username"을 모두 CustomOAuth2UserService로부터 받아옵니다.
        String userId = (String) oAuth2User.getAttributes().get("id");
        String username = (String) oAuth2User.getAttributes().get("username");

        // 3. 우리 앱의 JWT 토큰 생성 (userId와 username 모두 전달)
        String token = jwtTokenProvider.generateToken(userId, username);

        // 4. 프론트엔드로 보낼 URL 생성 (토큰 포함)
        String targetUrl = UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                .queryParam("token", token)
                .build().toUriString();

        // 5. 프론트엔드 URL로 리디렉션
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}