package com.sgcib.github.api.eventhandler.configuration;

import lombok.Getter;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@Component
public final class HandlerConfiguration {

    @Value("${handler.authorization.login}")
    private String login;

    @Value("${handler.authorization.password}")
    private String password;

    @Getter
    private HttpHeaders httpHeaders = new HttpHeaders();

    @PostConstruct
    private void setUp() {

        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        httpHeaders.set("Authorization", authHeader);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        login = null;
        password = null;
    }
}
