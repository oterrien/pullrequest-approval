package com.sgcib.github.api.eventhandler;

import org.springframework.http.HttpStatus;

@FunctionalInterface
public interface IEventHandler {

    HttpStatus handle(String event);
}
