package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.IEventHandler;
import com.sgcib.github.api.eventhandler.IssueCommentEventHandler;
import com.sgcib.github.api.eventhandler.PullRequestEventHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class EventFactory {

    private ApplicationContext applicationContext;

    public EventFactory() {
        this.applicationContext = new AnnotationConfigApplicationContext(
                PullRequestEventHandler.class,
                IssueCommentEventHandler.class,
                JSOnService.class
        );
    }

    public Optional<IEventHandler> getEventHandler(String event) {

        switch (EventType.of(event)) {
            case ISSUE_COMMENT:
                return Optional.of(applicationContext.getBean(IssueCommentEventHandler.class));
            case PULL_REQUEST:
                return Optional.of(applicationContext.getBean(PullRequestEventHandler.class));
            case NONE:
            default:
                return Optional.empty();
        }
    }

    enum EventType {

        PULL_REQUEST("pull_request"), ISSUE_COMMENT("issue_comment"), NONE("none");

        private String event;

        EventType(String event) {
            this.event = event;
        }

        public static EventType of(final String event) {
            return Stream.of(EventType.values()).
                    filter(p -> p.event.equals(event)).
                    findFirst().
                    orElse(NONE);
        }
    }
}
