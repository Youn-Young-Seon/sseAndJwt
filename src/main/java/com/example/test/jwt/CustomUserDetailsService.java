package com.example.test.jwt;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("test".equals(username)) {
            return User.builder()
                    .username("test")
                    .password("1234") // {noop}은 패스워드 인코딩 비활성화
                    .roles("USER")
                    .build();
        }
        if ("asdf".equals(username)) {
            return User.builder()
                    .username("asdf")
                    .password("1234") // {noop}은 패스워드 인코딩 비활성화
                    .roles("USER")
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }
}
