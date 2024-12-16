package com.example.test.api;

import com.example.test.config.SseEmitters;
import com.example.test.dto.AddResponseDto;
import com.example.test.dto.MessageDto;
import com.example.test.dto.ResponseDto;
import com.example.test.dto.User;
import com.example.test.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping("/api")
public class RestApiController {

//    private final RestTemplate restTemplate;
    private final SseEmitters sseEmitters;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

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

    @GetMapping(value = "/auth")
    public ResponseEntity<?> auth(HttpServletRequest request) {
        String authorizationToken = request.getHeader("Authorization").replace("Bearer ", "");
        String id = jwtUtil.generateToken(authorizationToken);
        User user = new User();
        user.setId(id);

        return ResponseEntity.ok().body(new ResponseDto<User>(user, HttpStatus.OK));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getId(), user.getPassword())
            );

            String token = jwtUtil.generateToken(user.getId());
            return ResponseEntity.ok().header("Authorization", "Bearer " + token).body(new ResponseDto<User>(user, HttpStatus.OK));
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    @GetMapping(value = "/sse", produces = "text/event-stream;charset=utf-8")
    public SseEmitter makeSse(HttpServletResponse response) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        String sseUuid = sseEmitters.add(sseEmitter);
        response.addCookie(new Cookie("sessionId", sseUuid));
        return sseEmitter;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addMessage(@RequestBody MessageDto messageDto) throws IOException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Map<String, SseEmitter> emitters = sseEmitters.getEmitters();

        executorService.execute(() -> {
            for (String s : emitters.keySet()) {
                SseEmitter sseEmitter = emitters.get(s);
                try {
                    sseEmitter.send(SseEmitter.event()
                            .id(String.valueOf(UUID.randomUUID()))
                            .name("message")
                            .data(messageDto)
                            .build());
                } catch (IOException e) {
                    log.info("thread: " + Thread.currentThread().getName());
                    sseEmitters.remove(messageDto.getId());
                }
            }
        });
        executorService.shutdown();

        return ResponseEntity.ok(messageDto);
    }
}
