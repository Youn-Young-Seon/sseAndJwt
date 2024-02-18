package com.example.test.handler;


import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class TestHandler {
    private final Sinks.Many<String> sink;

    public TestHandler() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public Mono<ServerResponse> getMono(ServerRequest request) {
        return ServerResponse.ok().body(Mono.just("test"), String.class);
    }

    public Mono<ServerResponse> getSse(ServerRequest request) {
        Flux<ServerSentEvent<String>> sseFlux = sink
                .asFlux()
                .map(message -> ServerSentEvent.builder(message)
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
}
