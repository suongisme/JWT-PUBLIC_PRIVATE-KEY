package com.example.demo.config;

import com.example.demo.dtos.SecureKey;
import io.jsonwebtoken.*;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Setter
public class JwtConfig {
    private String keyAlgorithm;
    private Long expire;

    private int keySize;

    @Bean
    public KeyHandler keyHandler() {
        return new KeyHandler();
    }

    @Bean
    public JwtHandler jwtHandler() {
        return new JwtHandler();
    }

    public class JwtHandler {

        private JwtHandler() {}

        public String genToken(UserDetails userDetails, Key privateKey) {
            Map<String, Object> claims = new HashMap<>() {{
                put("username", userDetails.getUsername());
                put("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray());
            }};

            return Jwts.builder()
                    .setSubject(userDetails.getUsername())
                    .setClaims(claims)
                    .signWith(privateKey)
                    .setExpiration(new Date(System.currentTimeMillis() + expire))
                    .compact();
        }

        @Deprecated
        public boolean isExpired(String token, Key publicKey) {
            Claims claims = this.getClaims(token, publicKey);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        }

        public UserDetails parseToken(String token, Key publicKey) {
            Claims claims = this.getClaims(token, publicKey);
            ArrayList<String> authorities = claims.get("authorities", ArrayList.class);
            return User.builder()
                    .username(claims.get("username", String.class))
                    .password("")
                    .authorities(authorities.toArray(String[]::new))
                    .build();
        }

        private Claims getClaims(String token, Key publicKey) {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

    }

    public class KeyHandler {

        private KeyHandler() {}

        @SneakyThrows
        @NonNull
        public SecureKey generateKey() {
            KeyPairGenerator instance = KeyPairGenerator.getInstance(keyAlgorithm);
            instance.initialize(keySize);
            KeyPair keyPair = instance.generateKeyPair();
            return SecureKey.builder()
                    .privateKey(keyPair.getPrivate())
                    .publicKey(keyPair.getPublic())
                    .build();
        }

        @SneakyThrows
        public java.security.Key getPublicKey(byte[] encodedKey) {
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            KeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedKey);
            return keyFactory.generatePublic(x509EncodedKeySpec);
        }
    }
}
