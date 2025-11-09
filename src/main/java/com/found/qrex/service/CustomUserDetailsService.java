package com.found.qrex.service;

import com.found.qrex.domain.User;
import com.found.qrex.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // ✅ 추가
import org.springframework.stereotype.Service;
import java.util.List; // ✅ 추가

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // ✅ 기본 권한 ROLE_USER 부여
        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getUserPw(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
