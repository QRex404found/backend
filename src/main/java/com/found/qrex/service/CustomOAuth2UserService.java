// package com.found.qrex.service;

package com.found.qrex.service;

import com.found.qrex.domain.User;
import com.found.qrex.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // ... (1~4단계: 유저 정보 파싱) ...
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerId = "";
        String nickname = "";
        String email = "";

        if ("kakao".equals(registrationId)) {
            providerId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            nickname = (String) profile.get("nickname");
            if (kakaoAccount.containsKey("email")) {
                email = (String) kakaoAccount.get("email");
            }
        }
        else if ("google".equals(registrationId)) {
            providerId = (String) attributes.get("sub");
            nickname = (String) attributes.get("name");
            email = (String) attributes.get("email");
        }
        String userId = registrationId + "_" + providerId;
        final String finalNickname = nickname;
        final String finalEmail = email;

        // 5. DB에서 유저 조회 또는 생성 (변경 없음)
        User user = userRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUserId(userId);
                    newUser.setUserName(finalNickname);
                    if (finalEmail != null && !finalEmail.isEmpty()) {
                        newUser.setEmail(finalEmail);
                    }
                    newUser.setUserPw(passwordEncoder.encode(UUID.randomUUID().toString()));
                    return userRepository.save(newUser);
                });

        // 6. Spring Security가 사용할 OAuth2User 객체 반환
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                // ⭐️ [핵심 수정]
                // "id" (userId) 뿐만 아니라, "username"도 맵에 추가하여 반환합니다.
                Map.of(
                        "id", user.getUserId(),
                        "username", user.getUserName()
                ),
                "id"
        );
    }
}