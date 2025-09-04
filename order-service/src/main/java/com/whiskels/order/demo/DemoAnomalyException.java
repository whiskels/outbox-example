package com.whiskels.order.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DemoAnomalyException extends RuntimeException {
    public DemoAnomalyException(String message) {
        super(message);
    }
}
