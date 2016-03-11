package com.sgcib.github.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by Olivier on 18/02/2016.
 */
@RestController
@SpringBootApplication
public class PullRequestController {

    @Autowired
    private EventFactory eventFactory;

    public static void main(String[] args) {
        SpringApplication.run(PullRequestController.class, args);
    }

    @RequestMapping(method = RequestMethod.POST, name = "/")
    public ResponseEntity<String> onEvent(RequestEntity request) {

        if (!request.hasBody())
            return new ResponseEntity("Empty Body", HttpStatus.UNPROCESSABLE_ENTITY);

        String event = request.getHeaders().getFirst("x-github-event");

        eventFactory.getEventHandler(event).
                ifPresent(h -> h.handle(request.getBody().toString()));

        return new ResponseEntity("OK", HttpStatus.OK);
    }
}

