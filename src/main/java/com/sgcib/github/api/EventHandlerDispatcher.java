package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.IEventHandler;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class EventHandlerDispatcher{

    @Autowired
    private IEventHandler issueCommentEventHandler;

    @Autowired
    private IEventHandler pullRequestEventHandler;

    private Optional<IEventHandler> getEventHandler(String event) {

        switch (Event.of(event)) {
            case ISSUE_COMMENT:
                return Optional.of(issueCommentEventHandler);
            case PULL_REQUEST:
                return Optional.of(pullRequestEventHandler);
            default:
                return Optional.empty();
        }
    }

    public ResponseEntity handle(String event, String body) {
        return getEventHandler(event)
                .map(eventHandler -> eventHandler.handle(body))
                .map(httpStatus -> new ResponseEntity<>(httpStatus.toString(), httpStatus))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED.toString(), HttpStatus.NOT_IMPLEMENTED));
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
