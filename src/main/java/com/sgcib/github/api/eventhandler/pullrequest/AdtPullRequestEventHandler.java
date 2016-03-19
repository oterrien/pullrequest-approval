package com.sgcib.github.api.eventhandler.pullrequest;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.configuration.RemoteConfiguration;
import com.sgcib.github.api.eventhandler.EventHandlerException;
import com.sgcib.github.api.json.PullRequest;
import com.sgcib.github.api.json.PullRequestEvent;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.service.ICommunicationService;
import com.sgcib.github.api.service.RemoteConfigurationException;
import com.sgcib.github.api.service.RemoteConfigurationService;
import com.sgcib.github.api.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public abstract class AdtPullRequestEventHandler implements IHandler<PullRequestEvent, HttpStatus> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ICommunicationService communicationService;

    protected final StatusService statusService;

    protected final RemoteConfigurationService remoteConfigurationService;

    protected final Configuration configuration;

    protected AdtPullRequestEventHandler(Configuration configuration, RemoteConfigurationService remoteConfigurationService, ICommunicationService communicationService, StatusService statusService) {

        this.configuration = configuration;
        this.remoteConfigurationService = remoteConfigurationService;
        this.communicationService = communicationService;
        this.statusService = statusService;
    }

    protected boolean isStateAlreadySet(PullRequestEvent event, Status.State targetState, String targetStatusContext) {

        Status.State currentState = statusService.getCurrentState(event.getPullRequest().getStatusesUrl(), targetStatusContext);
        if (currentState == targetState) {
            if (logger.isDebugEnabled()) {
                logger.debug("Status '" + targetStatusContext + "' for repository '" + event.getRepository().getName() + "' is currently set to " + targetState.getValue() + " -> no change");
            }
            return true;
        }
        return false;
    }

    protected HttpStatus postStatus(PullRequestEvent event, Status.State targetState, String targetStatusContext) {

        if (logger.isDebugEnabled()) {
            logger.debug("Status '" + targetStatusContext + "' for repository '" + event.getRepository().getName() + "' will be updated to " + targetState);
        }

        try {
            RemoteConfiguration remoteConfiguration = remoteConfigurationService.createRemoteConfiguration(event.getRepository());
            PullRequest pullRequest = event.getPullRequest();
            Status targetStatus = statusService.createStatus(targetState, pullRequest.getUser().getLogin(), targetStatusContext, remoteConfiguration);
            return communicationService.post(pullRequest.getStatusesUrl(), targetStatus);
        } catch (RemoteConfigurationException e) {
            throw new EventHandlerException(e, HttpStatus.PRECONDITION_FAILED, e.getMessage());
        }
    }
}
