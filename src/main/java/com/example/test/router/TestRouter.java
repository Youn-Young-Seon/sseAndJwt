package com.example.test.router;

import com.example.test.dto.User;
import com.example.test.handler.TestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

@Configuration
public class TestRouter {

    @Bean
    public RouterFunction<?> routeTest(TestHandler handler) {
        return RouterFunctions.route()
                .GET("/test", handler::getMono)
                .GET("/sse", handler::getSse)
                .GET("/auth", handler::auth)
                .POST("/add", handler::add)
                .POST("/login", handler::login)
                .build();
    }
}

