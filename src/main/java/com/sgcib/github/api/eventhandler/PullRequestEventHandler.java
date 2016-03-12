package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.payloayd.PullRequest;
import com.sgcib.github.api.payloayd.PullRequestPayload;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class PullRequestEventHandler extends AdtEventHandler<PullRequestPayload> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(PullRequestEventHandler.class);

    public PullRequestEventHandler() {
        super(PullRequestPayload.class);
    }

    @Override
    public HttpStatus handle(PullRequestPayload event) throws EventHandlerException {

        logger.info("Event received from repository '" + event.getRepository().getName() + "'");

        String action = event.getAction();

        if (logger.isDebugEnabled()) {
            logger.debug("Pull request action is '" + action + "'");
        }

        switch (Action.of(action)) {
            case OPENED:
            case SYNCHRONIZED:
                return this.postStatus(event, Status.State.PENDING);
        }

        return HttpStatus.OK;
    }

    private HttpStatus postStatus(PullRequestPayload event, Status.State state) throws EventHandlerException {

        if (logger.isDebugEnabled())
            logger.debug("Trying to set pull request's state to '" + state.getState() + "'");

        PullRequest pullRequest = event.getPullRequest();
        String statusesUrl = pullRequest.getStatusesUrl();
        Status status = generateStatus(state, Optional.of(pullRequest.getUser().getLogin()));

        return postStatus(statusesUrl, status);
    }

    public enum Action {
        CLOSED("closed"), OPENED("opened"), REOPENED("reopened"), SYNCHRONIZED("synchronize"), NONE("");

        @Getter
        private String value;

        Action(String value) {
            this.value = value;
        }

        public static Action of(final String value) {
            return Stream.of(Action.values()).
                    filter(p -> p.value.equals(value)).
                    findFirst().
                    orElse(NONE);
        }
    }
}
