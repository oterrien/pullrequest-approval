package com.sgcib.github.api.eventhandler.pullrequest;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.json.PullRequestEvent;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.service.ICommunicationService;
import com.sgcib.github.api.service.RemoteConfigurationService;
import com.sgcib.github.api.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PullRequestOpenedHandler extends AdtPullRequestEventHandler implements IHandler<PullRequestEvent, HttpStatus> {

    @Autowired
    public PullRequestOpenedHandler(Configuration configuration, RemoteConfigurationService remoteConfigurationService, ICommunicationService communicationService, StatusService statusService) {
        super(configuration, remoteConfigurationService, communicationService, statusService);
    }

    @Override
    public HttpStatus handle(PullRequestEvent event) {

        String targetStatusContext = configuration.getPullRequestApprovalStatusContext();
        Status.State targetState = Status.State.PENDING;

        if (isStateAlreadySet(event, targetState, targetStatusContext)) {
            return HttpStatus.OK;
        }

        return postStatus(event, targetState, targetStatusContext);

    }
}