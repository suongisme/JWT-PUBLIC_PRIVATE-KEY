package com.example.demo;

import com.example.demo.config.JwtConfig;
import com.example.demo.dtos.SecureKey;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TokenTest {

    @InjectMocks
    private JwtConfig jwtConfig;

    private JwtConfig.JwtHandler jwtHandler;

    private JwtConfig.KeyHandler keyHandler;

    private UserDetails userDetails;

    private AutoCloseable autoCloseable;

    private Long expire;

    @BeforeAll
    public void init() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);
        this.expire = 2_000L;
        this.jwtConfig.setExpire(this.expire);
        this.jwtConfig.setKeyAlgorithm("RSA");
        this.jwtConfig.setKeySize(2048);
        this.jwtHandler = this.jwtConfig.jwtHandler();
        this.keyHandler = this.jwtConfig.keyHandler();

        this.userDetails = User.builder()
                .username("suong")
                .password("suong")
                .authorities("USER")
                .build();
    }

    @Test
    public void genTokenTest() {
        SecureKey secureKey = this.keyHandler.generateKey();
        String token = this.jwtHandler.genToken(this.userDetails, secureKey.getPrivateKey());
        Assertions.assertNotNull(token);
    }

    @Test
    public void decodeJwtWithPublicKeyTest() {
        SecureKey secureKey = this.keyHandler.generateKey();
        String token = this.jwtHandler.genToken(userDetails, secureKey.getPrivateKey());
        UserDetails userDetails1 = this.jwtHandler.parseToken(token, secureKey.getPublicKey());

        Assertions.assertEquals(userDetails.getUsername(), userDetails1.getUsername());
    }

    @Test
    public void decodeExpiredJwtTest() {
        SecureKey secureKey = this.keyHandler.generateKey();
        String token = this.jwtHandler.genToken(userDetails, secureKey.getPrivateKey());
        Assertions.assertThrows(ExpiredJwtException.class, () -> {
            Thread.sleep(this.expire);
            this.jwtHandler.parseToken(token, secureKey.getPublicKey());
        });
    }

    @Test
    public void decodeWithFakePublicKeyTest() {
        byte[] fakePublicKey = new byte[]{48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -13, -107, -99, -61, -6, -95, -125, 29, -48, 75, -9, -56, 2, -18, -48, -107, -103, 33, 90, 97, 86, 59, -117, 84, -58, -99, -8, -7, -9, -110, -14, 47, -47, 2, -13, -97, -71, 89, 4, 86, 51, 64, -110, -43, 104, 55, 122, 16, -28, 113, 31, -80, -22, -83, -64, -42, 41, 22, -89, -90, -95, 89, 7, 1, -82, -59, 33, 79, -38, 53, -94, 28, -74, -25, -42, 81, 6, 9, 108, 33, 71, 96, 14, -112, -47, 104, -100, -121, -75, -106, 110, 32, -127, -19, -123, -77, -86, -1, 110, 43, -125, 22, -5, -27, 90, -106, -19, 1, -43, -2, -10, -118, -108, -42, -68, -49, -70, 64, 58, -3, 48, 127, 16, -104, -90, -88, -21, -18, -93, 23, -72, 10, 116, 36, -114, 77, 57, -57, 84, -111, -50, 78, -41, 21, 55, 124, -5, 34, -4, -98, -25, 106, 113, -108, -115, -7, 77, 20, 70, -4, 26, 26, -64, 72, 32, -41, 50, -33, -14, -19, -44, 3, -105, -58, 10, -18, -4, 73, 4, -41, 75, -46, -41, -18, -120, 125, -32, -54, 11, 92, 72, -71, 10, 58, -102, 26, -75, -34, -122, 47, 78, 2, 85, 61, 120, -54, 50, -24, 18, -21, -27, 40, 115, -77, 118, -127, -69, -84, 89, 60, -89, 92, 46, 49, 18, -62, -124, -21, 102, -71, 89, -102, -104, 22, -113, -3, -94, -16, -82, 56, -103, -13, -38, -98, 47, 64, -4, 44, 6, 28, 110, 91, -68, 55, 26, -43, 2, 3, 1, 0, 1};
        Key publicKey = this.keyHandler.getPublicKey(fakePublicKey);
        SecureKey secureKey = this.keyHandler.generateKey();
        String token = this.jwtHandler.genToken(this.userDetails, secureKey.getPrivateKey());
        Assertions.assertThrows(SignatureException.class, () -> {
            this.jwtHandler.parseToken(token, publicKey);
        });
    }

    @AfterAll
    public void destroy() throws Exception {
        this.autoCloseable.close();
    }
}
