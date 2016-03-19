package com.sgcib.github.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class PullRequestApprovalController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PullRequestApprovalController.class);

    @Autowired
    private EventHandlerDispatcher eventHandlerDispatcher;

    @RequestMapping(method = RequestMethod.POST)
    public final ResponseEntity<String> onEvent(@RequestBody String body, @RequestHeader HttpHeaders headers) {

        // TODO see the behavior when the repository is forked and private
        String event = headers.getFirst("x-github-event");

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Received event type : " + event);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("body : " + body);
        }

        return eventHandlerDispatcher.handle(event, body);
    }
}

