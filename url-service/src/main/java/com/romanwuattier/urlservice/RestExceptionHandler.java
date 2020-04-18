package com.romanwuattier.urlservice;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalServerError handle5xx(RuntimeException ex, WebRequest request) {
        return InternalServerError.builder()
                                  .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                  .message("unexpected_error")
                                  .build();
    }

    @Value
    @Builder
    static class InternalServerError {
        int code;
        String message;
    }
}
