package com.sgcib.github.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sgcib.github.api.component.ICommunicationService;
import com.sgcib.github.api.eventhandler.EventHandlerDispatcher;
import lombok.Data;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/webhook")
public final class PullRequestApprovalController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PullRequestApprovalController.class);

    @Autowired
    private EventHandlerDispatcher eventHandlerDispatcher;

    @RequestMapping(method = RequestMethod.POST)
    public final ResponseEntity<String> onEvent(@RequestBody String body, @RequestHeader HttpHeaders headers) {

        String event = headers.getFirst("x-github-event");

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Received event type : " + event);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("body : " + body.replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", ""));
        }

        return eventHandlerDispatcher.handle(event, body);
    }
}

