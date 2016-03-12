package com.sgcib.github.api.eventhandler;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class EventHandlerException extends Exception {

    private Exception innerException;

    private HttpStatus httpStatus;

    private String reason;

    private String event;

    public EventHandlerException(Exception innerException, HttpStatus httpStatus, String reason) {

        this.innerException = innerException;
        this.httpStatus = httpStatus;
        this.reason = reason;
    }

    public EventHandlerException(Exception innerException, HttpStatus httpStatus, String reason, String event) {

        this(innerException, httpStatus, reason);
        this.event = event;
    }
}