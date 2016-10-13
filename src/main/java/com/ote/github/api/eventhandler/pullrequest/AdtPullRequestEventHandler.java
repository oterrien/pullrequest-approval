package com.ote.github.api.eventhandler.pullrequest;

import com.ote.github.api.component.ICommunicationService;
import com.ote.github.api.component.IRepositoryConfigurationService;
import com.ote.github.api.eventhandler.AdtEventHandler;
import com.ote.github.api.eventhandler.IHandler;
import com.ote.github.api.json.PullRequest;
import com.ote.github.api.json.PullRequestEvent;
import com.ote.github.api.json.Repository;
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
