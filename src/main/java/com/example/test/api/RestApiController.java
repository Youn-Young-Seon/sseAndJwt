package com.example.test.api;

import com.example.test.config.SseEmitters;
import com.example.test.dto.AddResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RestApiController {

//    private final RestTemplate restTemplate;
    private final SseEmitters sseEmitters;

    @PostMapping("/async")
    public ResponseEntity<?> async() {
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
            double random = Math.random() * 5000;

            try {
                Thread.sleep((long) random);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            String stringRandom = String.valueOf(random);

            String jsonRandom = "{ \"key\": \"" + stringRandom + "\" }";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonRandom, headers);

//            restTemplate.postForEntity("http://localhost:8080//callback", requestEntity, String.class);
        });

        return ResponseEntity.ok("{ \"success\": \"Async task initiated. Result will be sent to the callback URL.\" }");
    }

    @GetMapping(value = "/sse", produces = "text/event-stream;charset=utf-8")
    public SseEmitter makeSse(HttpServletResponse response) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        String sseUuid = sseEmitters.add(sseEmitter);
        response.addCookie(new Cookie("sessionId", sseUuid));
        return sseEmitter;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addMessage(@RequestBody String data, HttpServletRequest request) throws IOException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Map<String, SseEmitter> emitters = sseEmitters.getEmitters();
        String selectCookie = null;

//        Cookie[] cookies = request.getCookies();
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equals("sessionId")) {
//                selectCookie = cookie.getValue();
//            }
//        }

        AddResponseDto addResponseDto = new AddResponseDto(data, selectCookie);

        executorService.execute(() -> {
            for (String s : emitters.keySet()) {
                SseEmitter sseEmitter = emitters.get(s);
                try {
                    sseEmitter.send(SseEmitter.event()
                            .id(String.valueOf(UUID.randomUUID()))
                            .name("message")
                            .data(addResponseDto)
                            .build());
                } catch (IOException e) {
                    log.info("thread: " + Thread.currentThread().getName());
                }
            }
        });
        executorService.shutdown();

        return ResponseEntity.ok(addResponseDto);
    }
}
