package com.romanwuattier.keygeneratorservice;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public InternalServerError handle5xx(RuntimeException ex, WebRequest request) {
        return InternalServerError.builder()
                                  .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                  .message(ex.getMessage())
                                  .build();
    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BadRequestError handle400(RuntimeException ex, WebRequest request) {
        return BadRequestError.builder()
                              .code(HttpStatus.BAD_REQUEST.value())
                              .parameters(request.getParameterMap())
                              .message(ex.getMessage())
                              .build();
    }

    @Value
    @Builder
    private static class InternalServerError {
        int code;
        String message;
    }

    @Value
    @Builder
    private static class BadRequestError {
        int code;
        Map<String, String[]> parameters;
        String message;
    }
}
