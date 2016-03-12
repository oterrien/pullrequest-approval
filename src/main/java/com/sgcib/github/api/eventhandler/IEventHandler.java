package com.sgcib.github.api.eventhandler;

import org.springframework.http.HttpStatus;

/**
 * Created by Olivier on 07/03/2016.
 */
public interface IEventHandler {

    HttpStatus handle(String event);
}
