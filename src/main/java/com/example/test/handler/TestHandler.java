package com.example.test.handler;


import com.example.test.dto.User;
import com.example.test.jwt.BearerToken;
import com.example.test.jwt.JwtSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class TestHandler {
    private final Sinks.Many<String> sink;
    private final ReactiveUserDetailsService users;
    private final JwtSupport jwtSupport;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TestHandler(ReactiveUserDetailsService users, JwtSupport jwtSupport, PasswordEncoder passwordEncoder) {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
        this.users = users;
        this.jwtSupport = jwtSupport;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<ServerResponse> getSse(ServerRequest request) {
        Flux<ServerSentEvent<String>> sseFlux = sink.asFlux()
                .map(message -> ServerSentEvent
                        .builder(message)
                        .build())
                .doOnCancel(() -> {
                    sink.asFlux().blockLast();
                });
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(sseFlux, ServerSentEvent.class);
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        return request.bodyToMono(String.class)
                .doOnNext(sink::tryEmitNext)
                .then(ServerResponse.ok().body(Mono.just("success"), String.class));
    }

    public Mono<ServerResponse> auth(ServerRequest request) {
        return ServerResponse.ok().body(Mono.just("ok"), String.class);
    }

//    public Mono<ServerResponse> loginPage(ServerRequest request) {
//        return ServerResponse.ok().contentType(MediaType.TEXT_HTML).render("login");
//    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(User.class)
                .flatMap(user -> {
                    Mono<UserDetails> foundUser = users.findByUsername(user.getId()).defaultIfEmpty(null);

                    return foundUser.flatMap(u -> {
                        if (u != null) {
                            if (passwordEncoder.matches(user.getPassword(), u.getPassword())) {
                                return Mono.just(jwtSupport.generate("test"))
                                        .flatMap(token -> ServerResponse.ok().bodyValue(token.getValue()));
                            }
                            return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue("Invalid credentials");
                        }
                        return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue("User not found. Please register");
                    });
                });
    }
}
