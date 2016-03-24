package com.sgcib.github.api.eventhandler;


import com.sgcib.github.api.component.*;
import com.sgcib.github.api.json.PullRequest;
import com.sgcib.github.api.json.Repository;
import com.sgcib.github.api.json.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

public abstract class AdtEventHandler<T extends Serializable> implements IHandler<T, HttpStatus> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ICommunicationService communicationService;

    private final IRepositoryConfigurationService repositoryConfigurationService;

    @Autowired
    protected StatusService statusService;

    @Autowired
    protected StatusConfiguration statusConfiguration;

    @Autowired
    protected IssueCommentConfiguration issueCommentConfiguration;

    @Autowired
    protected AuthorizationConfiguration authorizationConfiguration;

    protected AdtEventHandler(IRepositoryConfigurationService repositoryConfigurationService, ICommunicationService communicationService) {

        this.repositoryConfigurationService = repositoryConfigurationService;
        this.communicationService = communicationService;
    }

    protected RepositoryConfiguration getRepositoryConfiguration(Repository repository) {
        try {
            return this.repositoryConfigurationService.createRemoteConfiguration(repository);
        } catch (RepositoryConfigurationException e){
            throw new EventHandlerException(e, HttpStatus.PRECONDITION_FAILED, e.getMessage());
        }
    }

    protected HttpStatus postStatus(T event, Status.State targetState, String targetStatusContext, RepositoryConfiguration remoteConfiguration) {

        if (logger.isDebugEnabled()) {
            logger.debug("Status '" + targetStatusContext + "' for repository '" + getRepository(event).getName() + "' will be updated to " + targetState);
        }

        PullRequest pullRequest = getPullRequest(event);
        Status targetStatus = statusService.createStatus(targetState, pullRequest.getUser().getLogin(), targetStatusContext, remoteConfiguration);
        return communicationService.post(pullRequest.getStatusesUrl(), targetStatus);
    }

    protected abstract PullRequest getPullRequest(T event);

    protected abstract Repository getRepository(T event);
}
