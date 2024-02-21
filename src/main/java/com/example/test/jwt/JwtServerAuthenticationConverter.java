package com.example.test.jwt;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Mono.just(createBearerToken(authHeader.substring(7)));
        } else {
            // 처리할 코드 추가 (예: 로그인이 필요한 리소스에 접근 시)
            return Mono.empty();
        }
    }

    private static BearerToken createBearerToken(String jwt) {
        return new BearerToken(jwt);
    }

//    @Override
//    public Mono<Authentication> convert(ServerWebExchange exchange) {
//        return Mono.justOrEmpty(Objects
//                        .requireNonNull(exchange
//                                .getRequest()
//                                .getHeaders()
//                                .getFirst(HttpHeaders.AUTHORIZATION)
//                        ))
//                .filter(authHeader -> authHeader.startsWith("Bearer "))
//                .map(authHeader -> authHeader.substring(7))
//                .map(JwtServerAuthenticationConverter::createBearerToken);
//    }
}
