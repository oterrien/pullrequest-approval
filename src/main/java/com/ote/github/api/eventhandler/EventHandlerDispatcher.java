package com.ote.github.api.eventhandler;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class EventHandlerDispatcher {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerDispatcher.class);

    @Autowired
    private IHandler<String, HttpStatus> issueCommentEventHandlerDispatcher;

    @Autowired
    private IHandler<String, HttpStatus> pullRequestEventHandlerDispatcher;

    public ResponseEntity<String> handle(String event, String body) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Dispatching event to dedicated handler");
        }

        return getEventHandler(event)
                .map(eventHandler -> eventHandler.handle(body))
                .map(httpStatus -> new ResponseEntity<>(httpStatus.toString(), httpStatus))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED.toString(), HttpStatus.NOT_IMPLEMENTED));
    }

    private Optional<IHandler<String, HttpStatus>> getEventHandler(String eventString) {

        Event event = Event.of(eventString);

        switch (event) {
            case ISSUE_COMMENT:
                return Optional.of(issueCommentEventHandlerDispatcher);
            case PULL_REQUEST:
                return Optional.of(pullRequestEventHandlerDispatcher);
            default:
                return Optional.empty();
        }
    }

    public enum Event {

        PULL_REQUEST("pull_request"), ISSUE_COMMENT("issue_comment"), NONE("");

        @Getter
        private String value;

        Event(String value) {
            this.value = value;
        }

        public static Event of(final String value) {

            return Stream.of(Event.values()).
                    filter(p -> p.value.equals(value)).
                    findFirst().
                    orElse(NONE);
        }
    }
}
