package com.sgcib.github.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


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

    @RequestMapping(method = RequestMethod.POST, name = "/webhook")
    public ResponseEntity<String> onPostEvent(@RequestBody String body, @RequestHeader HttpHeaders headers) {

        String event = headers.getFirst("x-github-event");

       eventFactory.getEventHandler(event).
                ifPresent(h -> h.handle(body.toString()));

        return new ResponseEntity("OK", HttpStatus.OK);
    }
}

