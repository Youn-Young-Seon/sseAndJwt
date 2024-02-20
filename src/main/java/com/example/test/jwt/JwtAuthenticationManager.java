package com.example.test.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements AuthenticationManager {

    private final JwtSupport jwtSupport;
    private final ReactiveUserDetailsService users;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return Mono.justOrEmpty(authentication)
                .filter(auth -> auth instanceof BearerToken)
                .cast(BearerToken.class)
                .flatMap(jwt -> Mono.fromCallable(() -> validate(jwt)))
                .onErrorMap(error -> new InvalidBearerToken(error.getMessage()))
                .block(); // 블로킹 방식으로 결과를 얻음
    }

    private Authentication validate(BearerToken token) {
        String username = jwtSupport.getId(token);
        UserDetails user = Objects.requireNonNull(users.findByUsername(username).block());

        if (jwtSupport.isValid(token, user)) {
            return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
        }

        throw new IllegalArgumentException("Token is not valid");
    }

}
