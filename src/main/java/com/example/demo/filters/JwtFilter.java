package com.example.demo.filters;

import com.example.demo.config.JwtConfig;
import com.example.demo.entities.Token;
import com.example.demo.errorhandler.InvalidTokenException;
import com.example.demo.repositories.TokenRepository;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.security.Key;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";

    private final JwtConfig.JwtHandler jwtHandler;
    private final JwtConfig.KeyHandler keyHandler;
    private final TokenRepository tokenRepository;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            this.handleToken(request);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            this.handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    public void handleToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (!StringUtils.hasLength(header)) return;
        String bearerToken = header.replace("Bearer ", "");
        Token token = this.tokenRepository.findByToken(bearerToken)
                .orElseThrow(() -> new InvalidTokenException("Token in invalid"));
        Key publicKey = this.keyHandler.getPublicKey(token.getPublicKey());
        UserDetails userDetails = this.jwtHandler.parseToken(bearerToken, publicKey);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
