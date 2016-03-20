package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.service.*;
import com.sgcib.github.api.json.IssueCommentEvent;
import com.sgcib.github.api.json.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class IssueCommentRejectionHandler extends AdtIssueCommentEventHandler implements IHandler<IssueCommentEvent, HttpStatus> {

    @Autowired
    public IssueCommentRejectionHandler(Configuration configuration, IRemoteConfigurationService remoteConfigurationService, ICommunicationService communicationService, StatusService statusService) {
        super(configuration, remoteConfigurationService, communicationService, statusService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) {

        if (isTechnicalUserAction(event)){
            return HttpStatus.OK;
        }

        enrich(event);

        String targetStatusContext = configuration.getPullRequestApprovalStatusContext();
        Status.State targetState = Status.State.ERROR;

        if (isStateAlreadySet(event, targetState, targetStatusContext)) {
            return HttpStatus.OK;
        }

        return postStatus(event, targetState, targetStatusContext);
    }
}