package com.sgcib.github.api.component;

import lombok.Getter;
import org.apache.commons.codec.Charsets;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@Component
public class AuthorizationConfiguration {

    @Value("${handler.authorization.login}")
    @Getter
    private String technicalUserLogin;

    @Value("${handler.authorization.password}")
    private String technicalUserPassword;

    @Getter
    private HttpHeaders httpHeaders = new HttpHeaders();

    @PostConstruct
    private void setUp() {

        this.technicalUserPassword = new String(Base64.decodeBase64(technicalUserPassword.getBytes(Charsets.UTF_8)));

        String auth = technicalUserLogin + ":" + technicalUserPassword;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        this.httpHeaders.set("Authorization", authHeader);
        this.httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }
}
