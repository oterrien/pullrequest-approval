package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.eventhandler.configuration.Configuration;
import com.sgcib.github.api.eventhandler.configuration.RemoteConfiguration;
import com.sgcib.github.api.json.PullRequest;
import com.sgcib.github.api.json.PullRequestPayload;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class PullRequestEventHandler extends AdtEventHandler<PullRequestPayload> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(PullRequestEventHandler.class);

    @Autowired
    public PullRequestEventHandler(Configuration configuration, ICommunicationService communicationService) {
        super(PullRequestPayload.class, configuration, communicationService);
    }

    @Override
    public HttpStatus handle(PullRequestPayload event) throws EventHandlerException {

        if (logger.isInfoEnabled()) {
            logger.info("Event received");
        }

        String action = event.getAction();

        if (logger.isDebugEnabled()) {
            logger.debug("Pull request action is '" + action + "'");
        }

        switch (Action.of(action)) {
            case OPENED:
                if (logger.isDebugEnabled()) {
                    logger.debug("Pull request has just been opened -> set status to pending");
                }
                return this.tryPostStatus(event, Status.State.PENDING);
            case SYNCHRONIZED:

                String statusesUrl = event.getPullRequest().getStatusesUrl();

                switch (getCurrentState(statusesUrl)) {
                    case ERROR:
                    case FAILURE:
                        if (logger.isDebugEnabled()) {
                            logger.debug("Pull request is currently rejected -> no change");
                        }
                        break;
                    case PENDING:
                        if (logger.isDebugEnabled()) {
                            logger.debug("Pull request is currently pending -> no change");
                        }
                        break;
                    case SUCCESS:
                        if (logger.isDebugEnabled()) {
                            logger.debug("Pull request is currently approved -> reset status to pending");
                        }
                        return this.tryPostStatus(event, Status.State.PENDING);
                }
                break;
        }

        return HttpStatus.OK;
    }

    private HttpStatus tryPostStatus(PullRequestPayload event, Status.State state) throws EventHandlerException {

        if (logger.isDebugEnabled()) {
            logger.debug("Setting pull request state to '" + state.getValue() + "'");
        }

        PullRequest pullRequest = event.getPullRequest();
        String statusesUrl = pullRequest.getStatusesUrl();
        Optional<RemoteConfiguration> remoteConfiguration = getRemoteConfiguration(event.getRepository());

        Status status = new Status(state, pullRequest.getUser().getLogin(), configuration, remoteConfiguration);

        return communicationService.post(statusesUrl, status);
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
