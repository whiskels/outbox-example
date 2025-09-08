package com.whiskels.order.api.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Unexpected error", ex);
        // a small tweak to mark the current span as erroneous
        var span = Span.current();
        span.setStatus(StatusCode.ERROR);
        span.recordException(ex);
        String bodyOfResponse = "Unexpected error occurred";
        return super.handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
