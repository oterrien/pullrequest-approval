package com.sgcib.github.api;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class PullRequestApprovalController {

    protected static final Logger logger = LoggerFactory.getLogger(PullRequestApprovalController.class);

    @Autowired
    private EventFactory eventFactory;

    @RequestMapping(method = RequestMethod.POST)
    public final ResponseEntity<String> onEvent(@RequestBody String body, @RequestHeader HttpHeaders headers) {

        // TODO see the behavior when the repository is forked and private

        String event = headers.getFirst("x-github-event");

        if (logger.isInfoEnabled())
            logger.info("Received event type '" + event + "'");

        final Result result = new Result();
        eventFactory.getEventHandler(event).
                ifPresent(h -> result.setStatus(h.handle(body.toString())));

        return result.getResponse();
    }

    @Data
    private class Result {

        private HttpStatus status;

        public ResponseEntity getResponse() {
            return new ResponseEntity(status == HttpStatus.OK ? "OK" : "KO", status);
        }
    }
}
