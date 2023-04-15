package com.example.demo.dtos.request;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
