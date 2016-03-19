package com.sgcib.github.api.eventhandler;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class EventHandlerException extends RuntimeException {

    private HttpStatus httpStatus;

    private String reason;

    public EventHandlerException(Exception innerException, HttpStatus httpStatus, String reason) {

        this.initCause(innerException);
        this.httpStatus = httpStatus;
        this.reason = reason;
    }
}