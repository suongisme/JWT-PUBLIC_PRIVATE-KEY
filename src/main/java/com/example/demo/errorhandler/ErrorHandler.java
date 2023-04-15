package com.example.demo.errorhandler;

import com.example.demo.dtos.ResponseData;
import com.example.demo.services.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorHandler {

    private final UserService userService;

    @ExceptionHandler({AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseData<String> unauthException(AuthenticationException ex) {
        ResponseData<String> responseData = new ResponseData<>();
        responseData.setStatus(HttpStatus.UNAUTHORIZED.value());
        responseData.setMessages(ex.getMessage());
        responseData.setCode(HttpStatus.UNAUTHORIZED.name());
        return responseData;
    }

    @ExceptionHandler({ExpiredJwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseData<String> expiredJwtException(ExpiredJwtException ex) {
        String username = ex.getClaims().get("username", String.class);
        this.userService.deleteTokenAsync(username);
        return this.invalidTokenException(new InvalidTokenException("Token is invalid"));
    }

    @ExceptionHandler({InvalidTokenException.class})
    public ResponseData<String> invalidTokenException(InvalidTokenException ex) {
        ResponseData<String> responseData = new ResponseData<>();
        responseData.setStatus(HttpStatus.UNAUTHORIZED.value());
        responseData.setMessages(ex.getMessage());
        responseData.setCode("INVALID_TOKEN");
        return responseData;
    }

}
