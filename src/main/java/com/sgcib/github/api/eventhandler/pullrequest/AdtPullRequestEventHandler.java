package com.sgcib.github.api.eventhandler.pullrequest;

import com.sgcib.github.api.eventhandler.AdtEventHandler;
import com.sgcib.github.api.eventhandler.IHandler;
import com.sgcib.github.api.component.*;
import com.sgcib.github.api.json.PullRequest;
import com.sgcib.github.api.json.PullRequestEvent;
import com.sgcib.github.api.json.Repository;
import org.springframework.http.HttpStatus;

public abstract class AdtPullRequestEventHandler extends AdtEventHandler<PullRequestEvent> implements IHandler<PullRequestEvent, HttpStatus> {

    public AdtPullRequestEventHandler(IRepositoryConfigurationService remoteConfigurationService, ICommunicationService communicationService) {
        super(remoteConfigurationService, communicationService);
    }

    @Override
    protected PullRequest getPullRequest(PullRequestEvent event) {
        return event.getPullRequest();
    }

    @Override
    protected Repository getRepository(PullRequestEvent event) {
        return event.getRepository();
    }
}
