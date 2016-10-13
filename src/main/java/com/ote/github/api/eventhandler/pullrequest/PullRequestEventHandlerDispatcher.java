package com.ote.github.api.eventhandler.pullrequest;

import com.ote.github.api.eventhandler.AdtEventHandlerDispatcher;
import com.ote.github.api.eventhandler.IHandler;
import com.ote.github.api.json.PullRequestEvent;
import com.ote.github.api.json.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PullRequestEventHandlerDispatcher extends AdtEventHandlerDispatcher<PullRequestEvent> implements IHandler<String, HttpStatus> {

    @Autowired
    private IHandler<PullRequestEvent, HttpStatus> pullRequestOpenedHandler;

    @Autowired
    private IHandler<PullRequestEvent, HttpStatus> pullRequestSynchronizedHandler;

    @Autowired
    private IHandler<PullRequestEvent, HttpStatus> pullRequestLabeledHandler;

    public PullRequestEventHandlerDispatcher() {
        super(PullRequestEvent.class);
    }

    @Override
    public HttpStatus handle(PullRequestEvent event) {

        String action = event.getAction();

        if (logger.isInfoEnabled()) {
            logger.info("Handling pull-request action : " + action);
        }

        return dispatch(event, action);
    }

    private HttpStatus dispatch(PullRequestEvent event, String action) {

        switch (PullRequestEventAction.of(action)) {
            case OPENED:
                return pullRequestOpenedHandler.handle(event);
            case SYNCHRONIZED:
                return pullRequestSynchronizedHandler.handle(event);
            case LABELED:
            case UNLABELED:
                return pullRequestLabeledHandler.handle(event);
        }

        return HttpStatus.OK;
    }

    @Override
    protected User getUser(PullRequestEvent event) {

        return event.getPullRequest().getUser();
    }

}
