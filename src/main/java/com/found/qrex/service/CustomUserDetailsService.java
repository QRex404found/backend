package com.found.qrex.service;

import com.found.qrex.domain.User;
import com.found.qrex.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // 👈 [추가] 권한 리스트용

/**
 * Spring Security가 사용자 인증 시 호출할 서비스입니다.
 * UserDetailsService 인터페이스를 구현합니다.
 */
@Service // 👈 1. 이 클래스를 Spring Bean으로 등록
public class CustomUserDetailsService implements UserDetailsService { // 👈 2. 인터페이스 구현

    private final UserRepository userRepository;

    // 3. UserRepository를 주입받음
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Spring Security가 'username' (여기서는 userId)으로 사용자를 찾을 때 호출됩니다.
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        // 4. DB에서 userId로 사용자를 찾습니다.
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // 5. DB에서 찾은 User 객체를 Spring Security가 이해하는 UserDetails 객체로 변환합니다.
        // (이 프로젝트는 별도 권한(Role)이 없으므로, 권한 리스트는 비워둡니다.)
        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),       // 사용자 ID (username)
                user.getUserPw(),       // 암호화된 비밀번호
                new ArrayList<>()      // 권한 목록 (비어있음)
        );
    }
}
