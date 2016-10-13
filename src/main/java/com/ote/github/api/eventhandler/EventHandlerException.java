package com.ote.github.api.eventhandler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = false)
public class EventHandlerException extends RuntimeException {

    private HttpStatus httpStatus;

    private String reason;

    public EventHandlerException(Exception innerException, HttpStatus httpStatus, String reason) {

        this.initCause(innerException);
        this.httpStatus = httpStatus;
        this.reason = reason;
    }
}