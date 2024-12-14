package com.example.test.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddResponseDto {
    private String data;
    private String sessionId;
}
