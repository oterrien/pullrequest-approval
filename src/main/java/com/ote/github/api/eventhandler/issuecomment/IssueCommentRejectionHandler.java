package com.ote.github.api.eventhandler.issuecomment;

import com.ote.github.api.component.ICommunicationService;
import com.ote.github.api.component.IRepositoryConfigurationService;
import com.ote.github.api.component.RepositoryConfiguration;
import com.ote.github.api.eventhandler.IHandler;
import com.ote.github.api.json.IssueCommentEvent;
import com.ote.github.api.json.Status;
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

        if (!isUserAuthorized(event.getRepository(), event.getComment().getUser())) {
            if (logger.isDebugEnabled()) {
                logger.debug("User " + event.getComment().getUser().getLogin() + " is not authorized to reject pull request for repository '" + event.getRepository().getName() + "'");
            }
            return HttpStatus.UNAUTHORIZED;
        }

        enrich(event);

        String targetStatusContext = statusConfiguration.getContextPullRequestApprovalStatus();
        Status.State targetState = Status.State.ERROR;

        RepositoryConfiguration repositoryConfiguration = getRepositoryConfiguration(event.getRepository());
        return postStatus(event, targetState, targetStatusContext, repositoryConfiguration);
    }
}