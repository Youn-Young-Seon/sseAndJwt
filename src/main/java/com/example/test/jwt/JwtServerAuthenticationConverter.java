package com.example.test.jwt;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(Objects
                        .requireNonNull(exchange
                                .getRequest()
                                .getHeaders()
                                .getFirst(HttpHeaders.AUTHORIZATION)
                        ))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .map(JwtServerAuthenticationConverter::createBearerToken);
    }

    private static BearerToken createBearerToken(String jwt) {
        return new BearerToken(jwt);
    }

}
