package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.IEventHandler;
import com.sgcib.github.api.eventhandler.IssueCommentEventHandler;
import com.sgcib.github.api.eventhandler.PullRequestEventHandler;
import com.sgcib.github.api.eventhandler.Service;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class EventFactory {

    private ApplicationContext applicationContext;

    public EventFactory() {
        applicationContext = new AnnotationConfigApplicationContext(
                PullRequestEventHandler.class,
                IssueCommentEventHandler.class,
                Service.class
        );
    }

    public Optional<IEventHandler> getEventHandler(String param) {

        if ("pull-request".equals(param)) {
            return Optional.of(applicationContext.getBean(PullRequestEventHandler.class));
        }

        if ("issue-comment".equals(param)) {
            return Optional.of(applicationContext.getBean(IssueCommentEventHandler.class));
        }

        return Optional.empty();
    }
}
