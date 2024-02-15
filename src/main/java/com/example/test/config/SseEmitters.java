package com.example.test.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
@Slf4j
public class SseEmitters {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<String, SseEmitter>();

    public String add(SseEmitter sseEmitter) {
        String uuid = String.valueOf(UUID.randomUUID());
        emitters.put(uuid, sseEmitter);
        emitterConfig(uuid, sseEmitter);
        return uuid;
    }

    private void emitterConfig(String uuid, SseEmitter sseEmitter) {

        sseEmitter.onCompletion(() -> {
            emitters.remove(uuid);
        });
        sseEmitter.onTimeout(() -> {
            log.info("sseEmitter timeout");
            sseEmitter.complete();
        });
        sseEmitter.onError((ex) -> {
            log.info("{}, thread: {}", ex.getMessage(), Thread.currentThread().getName());
            sseEmitter.complete();
        });
    }
}
