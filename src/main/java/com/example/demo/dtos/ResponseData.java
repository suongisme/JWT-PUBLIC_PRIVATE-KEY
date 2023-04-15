package com.example.demo.dtos;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ResponseData<T> {
    private int status;
    private String code;
    private String messages;
    private T data;

    public static <T> ResponseData<T> success(T data) {
        ResponseData<T> result = new ResponseData<>();
        result.setData(data);
        result.setStatus(HttpStatus.OK.value());
        result.setMessages("Success");
        result.setCode("");
        return result;
    }
}
