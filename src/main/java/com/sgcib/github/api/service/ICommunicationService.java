package com.sgcib.github.api.service;

import com.sgcib.github.api.eventhandler.EventHandlerException;
import org.springframework.http.HttpStatus;

/**
 * Created by Olivier on 15/03/2016.
 */
public interface ICommunicationService {

    <T> HttpStatus post(String url, T object) throws EventHandlerException;

    String get(String url) throws EventHandlerException;

    <T> T get(String url, Class<T> type) throws EventHandlerException;
}
