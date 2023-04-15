package com.example.demo.services;

import com.example.demo.config.JwtConfig;
import com.example.demo.dtos.SecureKey;
import com.example.demo.dtos.request.AuthRequest;
import com.example.demo.dtos.response.AuthResponse;
import com.example.demo.entities.Authority;
import com.example.demo.entities.Token;
import com.example.demo.entities.User;
import com.example.demo.repositories.TokenRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final JwtConfig.JwtHandler jwtHandler;
    private final JwtConfig.KeyHandler keyHandler;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("not found username: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getAuthorities().stream().map(Authority::getName).toArray(String[]::new))
                .build();
    }

    @Transactional
    public AuthResponse handleAfterAuthenticated(AuthRequest authRequest) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecureKey secureKey = this.keyHandler.generateKey();
        String jwt = this.jwtHandler.genToken(principal, secureKey.getPrivateKey());

        Token token = this.tokenRepository.findByUsername(authRequest.getUsername())
                .orElse(new Token());

        this.tokenRepository.save(token
                .setToken(jwt)
                .setPublicKey(secureKey.getPublicKey().getEncoded())
                .setUsername(principal.getUsername())
        );

        return AuthResponse.builder()
                .token(jwt)
                .build();
    }

    @Async
    @Transactional
    public void deleteTokenAsync(String username) {
        this.tokenRepository.deleteByUsername(username);
    }
}
