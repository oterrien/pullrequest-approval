package com.sgcib.github.api;

import lombok.Data;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Created by Olivier on 18/02/2016.
 */
@RestController
@SpringBootApplication
public class PullRequestController {

    protected static final Logger logger = LoggerFactory.getLogger(PullRequestController.class);

    @Autowired
    @Lazy(false)
    private EventFactory eventFactory;

    public static void main(String[] args) {
        SpringApplication.run(PullRequestController.class, args);
    }

    @RequestMapping(method = RequestMethod.POST, name = "/webhook")
    public final ResponseEntity<String> onEvent(@RequestBody String body, @RequestHeader HttpHeaders headers) {

        String event = headers.getFirst("x-github-event");

        if (logger.isInfoEnabled())
            logger.info("Received event type '" + event + "'");

        final Result result = new Result();
        eventFactory.getEventHandler(event).
                ifPresent(h -> result.setStatus(h.handle(body.toString())));

        return result.getResponse();
    }

    private class Result {

        @Setter
        private HttpStatus status;

        public ResponseEntity getResponse() {
            return new ResponseEntity(status == HttpStatus.OK ? "OK" : "KO", status);
        }
    }
}

