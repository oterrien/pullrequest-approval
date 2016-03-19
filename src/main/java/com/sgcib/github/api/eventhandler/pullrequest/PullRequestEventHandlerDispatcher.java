package com.sgcib.github.api.eventhandler.pullrequest;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.eventhandler.AdtEventHandlerDispatcher;
import com.sgcib.github.api.json.PullRequestEvent;
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

    @Autowired
    public PullRequestEventHandlerDispatcher(Configuration configuration) {
        super(PullRequestEvent.class, configuration);
    }

    @Override
    public HttpStatus handle(PullRequestEvent event) {

        String action = event.getAction();

        if (logger.isDebugEnabled()) {
            logger.debug("Pull request action is '" + action + "'");
        }

        switch (PullRequestEventAction.of(action)) {
            case OPENED:
                return pullRequestOpenedHandler.handle(event);
            case SYNCHRONIZED:
                return pullRequestSynchronizedHandler.handle(event);
            case LABELED:
                return pullRequestLabeledHandler.handle(event);
        }

        return HttpStatus.OK;
    }
}
