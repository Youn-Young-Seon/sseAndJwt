package com.example.test.config;

//@Getter
//@Component
//@Slf4j
public class SseEmitters {
//
//    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<String, SseEmitter>();
//
//    public String add(SseEmitter sseEmitter) {
//        String uuid = String.valueOf(UUID.randomUUID());
//        emitters.put(uuid, sseEmitter);
//        emitterConfig(uuid, sseEmitter);
//        return uuid;
//    }
//
//    private void emitterConfig(String uuid, SseEmitter sseEmitter) {
//
//        sseEmitter.onCompletion(() -> {
//            emitters.remove(uuid);
//        });
//        sseEmitter.onTimeout(() -> {
//            log.info("sseEmitter timeout");
//            sseEmitter.complete();
//        });
//        sseEmitter.onError((ex) -> {
//            log.info("{}, thread: {}", ex.getMessage(), Thread.currentThread().getName());
//            sseEmitter.complete();
//        });
//    }
}
