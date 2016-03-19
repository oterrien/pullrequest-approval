package com.sgcib.github.api;

import com.sgcib.github.api.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/webhook")
public class PullRequestApprovalController {

    protected static final Logger logger = LoggerFactory.getLogger(PullRequestApprovalController.class);

    @Autowired
    private EventHandlerDispatcher eventHandlerDispatcher;

    @RequestMapping(method = RequestMethod.POST)
    public final ResponseEntity<String> onEvent(@RequestBody String body, @RequestHeader HttpHeaders headers) {

        // TODO see the behavior when the repository is forked and private
        String event = headers.getFirst("x-github-event");

        if (logger.isInfoEnabled()) {
            logger.info("Received event type : " + event);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("body : " + body);
        }

        return eventHandlerDispatcher.handle(event, body);
    }
}

