package com.sgcib.github.api.eventhandler.pullrequest;

import com.sgcib.github.api.component.ICommunicationService;
import com.sgcib.github.api.component.IRepositoryConfigurationService;
import com.sgcib.github.api.component.RepositoryConfiguration;
import com.sgcib.github.api.eventhandler.IHandler;
import com.sgcib.github.api.json.PullRequestEvent;
import com.sgcib.github.api.json.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PullRequestSynchronizedHandler extends AdtPullRequestEventHandler implements IHandler<PullRequestEvent, HttpStatus> {

    @Autowired
    private IHandler<PullRequestEvent, HttpStatus> pullRequestLabeledHandler;

    @Autowired
    public PullRequestSynchronizedHandler(IRepositoryConfigurationService remoteConfigurationService, ICommunicationService communicationService) {
        super(remoteConfigurationService, communicationService);
    }

    @Override
    public HttpStatus handle(PullRequestEvent event) {

        String targetStatusContext = statusConfiguration.getContextPullRequestApprovalStatus();
        Status.State targetState = Status.State.PENDING;

        pullRequestLabeledHandler.handle(event);

        RepositoryConfiguration repositoryConfiguration = getRepositoryConfiguration(event.getRepository());
        return postStatus(event, targetState, targetStatusContext, repositoryConfiguration);
    }
}
