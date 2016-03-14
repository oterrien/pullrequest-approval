package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.json.PullRequest;
import com.sgcib.github.api.json.PullRequestPayload;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class PullRequestEventHandler extends AdtEventHandler<PullRequestPayload> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(PullRequestEventHandler.class);

    public PullRequestEventHandler() {
        super(PullRequestPayload.class);
    }

    @Override
    public HttpStatus handle(PullRequestPayload event) throws EventHandlerException {

        String remoteRepositoryName = event.getRepository().getName();

        if (logger.isInfoEnabled()) {
            logger.info(remoteRepositoryName + " : event received");
        }

        String action = event.getAction();

        if (logger.isDebugEnabled()) {
            logger.debug(remoteRepositoryName + " : pull request action is '" + action + "'");
        }

        switch (Action.of(action)) {
            case OPENED:
                if (logger.isDebugEnabled()) {
                    logger.debug(remoteRepositoryName + " : pull request has just been opened -> set status to pending");
                }
                return this.postStatus(event, Status.State.PENDING, remoteRepositoryName);
            case SYNCHRONIZED:

                String statusesUrl = event.getPullRequest().getStatusesUrl();

                switch (getCurrentState(statusesUrl, remoteRepositoryName)) {
                    case ERROR:
                    case FAILURE:
                        if (logger.isDebugEnabled()) {
                            logger.debug(remoteRepositoryName + " : pull request is currently rejected -> no change");
                        }
                        break;
                    case PENDING:
                        if (logger.isDebugEnabled()) {
                            logger.debug(remoteRepositoryName + " : pull request is currently pending -> no change");
                        }
                        break;
                    case SUCCESS:
                        if (logger.isDebugEnabled()) {
                            logger.debug(remoteRepositoryName + " : pull request is currently approved -> reset status to pending");
                        }
                        return this.postStatus(event, Status.State.PENDING, remoteRepositoryName);
                }
                break;
        }

        return HttpStatus.OK;
    }

    private HttpStatus postStatus(PullRequestPayload event, Status.State state, String remoteRepositoryName) throws EventHandlerException {

        if (logger.isDebugEnabled()) {
            logger.debug(remoteRepositoryName + " : setting pull request state to '" + state.getState() + "'");
        }

        PullRequest pullRequest = event.getPullRequest();
        String statusesUrl = pullRequest.getStatusesUrl();
        Status status = state.createStatus(pullRequest.getUser().getLogin());

        return communicationService.post(statusesUrl, status, remoteRepositoryName);
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
