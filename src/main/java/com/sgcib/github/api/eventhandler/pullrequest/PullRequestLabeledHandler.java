package com.sgcib.github.api.eventhandler.pullrequest;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.eventhandler.EventHandlerException;
import com.sgcib.github.api.json.Issue;
import com.sgcib.github.api.json.PullRequestEvent;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PullRequestLabeledHandler extends AdtPullRequestEventHandler implements IHandler<PullRequestEvent, HttpStatus> {

    @Autowired
    public PullRequestLabeledHandler(Configuration configuration, IRemoteConfigurationService remoteConfigurationService, ICommunicationService communicationService, StatusService statusService) {
        super(configuration, remoteConfigurationService, communicationService, statusService);
    }

    @Override
    public HttpStatus handle(PullRequestEvent event) {

        String targetStatusContext = configuration.getDoNotMergeLabelStatusContext();

        String doNotMergeLabelName = getDoNotMergeLabelName(event);
        Issue issue = communicationService.get(event.getPullRequest().getIssueUrl(), Issue.class);

        if (issue.getLabels().stream().anyMatch(l -> l.getName().equals(doNotMergeLabelName))) {

            Status.State targetState = Status.State.ERROR;

            if (isStateAlreadySet(event, targetState, targetStatusContext)) {
                return HttpStatus.OK;
            }

            return postStatus(event, targetState, targetStatusContext);
        } else {

            Status.State targetState = Status.State.SUCCESS;

            if (isStateAlreadySet(event, targetState, targetStatusContext)) {
                return HttpStatus.OK;
            }

            return postStatus(event, targetState, targetStatusContext);
        }
    }

    private String getDoNotMergeLabelName(PullRequestEvent event) {
        try {
            return remoteConfigurationService.createRemoteConfiguration(event.getRepository()).getDoNotMergeLabelName();
        } catch (RemoteConfigurationException e) {
            throw new EventHandlerException(e, HttpStatus.PRECONDITION_FAILED, e.getMessage());
        }
    }
}
