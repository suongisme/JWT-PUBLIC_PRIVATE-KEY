package com.example.demo.dtos;

import lombok.Builder;
import lombok.Data;

import java.security.Key;

@Data
@Builder
public class SecureKey {

    private Key publicKey;
    private Key privateKey;
}
