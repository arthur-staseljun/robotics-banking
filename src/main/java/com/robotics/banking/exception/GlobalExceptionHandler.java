package com.robotics.banking.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger EXCEPTION_LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExternalLogException.class)
    public ResponseEntity<ProblemDetail> handleExternalLogException(ExternalLogException ex) {
        EXCEPTION_LOGGER.warn(ex.getMessage(), ex);
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        body.setTitle("External Service Error");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @Override
    @Nullable
    @SuppressWarnings("java:S2638")
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var fieldErrorList = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(), "message", message(error)))
                .toList();
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        body.setTitle("Validation Error");
        body.setProperty("errors", fieldErrorList);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    @Nullable
    @SuppressWarnings("java:S2638")
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        body.setTitle("Validation Error");
        body.setProperty("errors", parameterErrors(ex));
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    private List<Map<String, String>> parameterErrors(HandlerMethodValidationException ex) {
        return ex.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> Map.of(
                                "field", parameterName(result.getMethodParameter()), "message", message(error)))
                ).toList();
    }

    private String parameterName(MethodParameter parameter) {
        String name = parameter.getParameterName();
        return name != null ? name : "parameter";
    }

    private String message(MessageSourceResolvable error) {
        String message = error.getDefaultMessage();
        return message != null ? message : "invalid value";
    }
}
