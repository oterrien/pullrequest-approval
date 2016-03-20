package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.eventhandler.IHandler;
import com.sgcib.github.api.json.IssueCommentEvent;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.component.ICommunicationService;
import com.sgcib.github.api.component.IRepositoryConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class IssueCommentRejectionHandler extends AdtIssueCommentEventHandler implements IHandler<IssueCommentEvent, HttpStatus> {

    @Autowired
    public IssueCommentRejectionHandler(IRepositoryConfigurationService remoteConfigurationService, ICommunicationService communicationService) {
        super(remoteConfigurationService, communicationService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) {

        if (isTechnicalUserAction(event)) {
            return HttpStatus.OK;
        }

        enrich(event);

        String targetStatusContext = statusConfiguration.getContextPullRequestApprovalStatus();
        Status.State targetState = Status.State.ERROR;

        if (isStateAlreadySet(event, targetState, targetStatusContext)) {
            return HttpStatus.OK;
        }

        return postStatus(event, targetState, targetStatusContext);
    }
}