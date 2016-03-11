package com.sgcib.github.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;


/**
 * Created by Olivier on 18/02/2016.
 */
@RestController
@SpringBootApplication
public class PullRequestController {

    private static Logger logger = Logger.getLogger(PullRequestController.class.getName());

    @Autowired
    private EventFactory eventFactory;

    public static void main(String[] args) {
        SpringApplication.run(PullRequestController.class, args);
    }

    @RequestMapping(method = RequestMethod.POST, name = "/webhook")
    public ResponseEntity<String> onPostEvent(@RequestBody String body, @RequestHeader HttpHeaders headers) {

        String event = headers.getFirst("x-github-event");

        eventFactory.getEventHandler(event).ifPresent(h -> h.handle(body.toString()));

        return new ResponseEntity("OK", HttpStatus.OK);
    }
}

