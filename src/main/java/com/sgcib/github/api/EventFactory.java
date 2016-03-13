package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.IEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class EventFactory {

    @Autowired
    private IEventHandler issueCommentEventHandler;

    @Autowired
    private IEventHandler pullRequestEventHandler;

    public Optional<IEventHandler> getEventHandler(String event) {

        switch (Event.of(event)) {
            case ISSUE_COMMENT:
                return Optional.of(issueCommentEventHandler);
            case PULL_REQUEST:
                return Optional.of(pullRequestEventHandler);
            default:
                return Optional.empty();
        }
    }

    public enum Event {

        PULL_REQUEST("pull_request"), ISSUE_COMMENT("issue_comment"), NONE("");

        private String event;

        Event(String event) {
            this.event = event;
        }

        public static Event of(final String event) {
            return Stream.of(Event.values()).
                    filter(p -> p.event.equals(event)).
                    findFirst().
                    orElse(NONE);
        }
    }
}
