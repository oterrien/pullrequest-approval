package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.eventhandler.IHandler;
import com.sgcib.github.api.component.*;
import com.sgcib.github.api.json.IssueCommentEvent;
import com.sgcib.github.api.json.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class IssueCommentPendingHandler extends AdtIssueCommentEventHandler implements IHandler<IssueCommentEvent, HttpStatus> {

    @Autowired
    public IssueCommentPendingHandler(IRepositoryConfigurationService remoteConfigurationService, ICommunicationService communicationService) {
        super(remoteConfigurationService, communicationService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) {

        if (!isUserAuthorized(event.getRepository(), event.getComment().getUser())){
            if (logger.isDebugEnabled()) {
                logger.debug("User "+ event.getComment().getUser().getLogin() +" is not authorized to change status of pull request for repository '" + event.getRepository().getName() + "'");
            }
            return HttpStatus.UNAUTHORIZED;
        }

        enrich(event);

        String targetStatusContext = statusConfiguration.getContextPullRequestApprovalStatus();
        Status.State targetState = Status.State.PENDING;

        return postStatus(event, targetState, targetStatusContext);
    }
}