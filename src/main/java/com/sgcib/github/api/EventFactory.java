package com.sgcib.github.api;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class EventFactory {

    private ApplicationContext applicationContext;

    public EventFactory() {
        applicationContext = new AnnotationConfigApplicationContext(
                PullRequestEventHandler.class,
                PullRequestReviewEventHandler.class,
                Service.class
        );
    }

    public void handle(String param) {

        if ("pull-request".equals(param)) {
            applicationContext.getBean(PullRequestEventHandler.class).handle(param);
        } else {
            applicationContext.getBean(PullRequestReviewEventHandler.class).handle(param);
        }


    }
}
