package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.configuration.RemoteConfiguration;
import com.sgcib.github.api.json.PullRequest;
import com.sgcib.github.api.json.PullRequestEvent;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.service.ICommunicationService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class PullRequestEventHandler extends AdtEventHandler<PullRequestEvent> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(PullRequestEventHandler.class);

    @Autowired
    public PullRequestEventHandler(Configuration configuration, ICommunicationService communicationService) {
        super(PullRequestEvent.class, configuration, communicationService);
    }

    @Override
    public HttpStatus handle(PullRequestEvent event) throws EventHandlerException {

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
                Status.State currentState = getCurrentState(statusesUrl);

                if (logger.isDebugEnabled() && currentState != Status.State.NONE) {
                    logger.debug("Pull request is currently " + currentState.getDescription() + " -> reset status to pending");
                }

                if (currentState == Status.State.SUCCESS) {
                    return this.tryPostStatus(event, Status.State.PENDING);
                }
                break;
        }

        return HttpStatus.OK;
    }

    private HttpStatus tryPostStatus(PullRequestEvent event, Status.State targetState) throws EventHandlerException {

        if (logger.isDebugEnabled()) {
            logger.debug("Setting pull request state to '" + targetState.getValue() + "'");
        }

        Optional<RemoteConfiguration> remoteConfiguration = getRemoteConfiguration(event.getRepository());
        PullRequest pullRequest = event.getPullRequest();

        // TODO duplication with PostStatusProcessor.process()
        String statusesUrl = pullRequest.getStatusesUrl();
        Status targetStatus = new Status(targetState, pullRequest.getUser().getLogin(), configuration, remoteConfiguration);
        return communicationService.post(statusesUrl, targetStatus);
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
