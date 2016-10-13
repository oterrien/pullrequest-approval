package com.ote.github.api.eventhandler.pullrequest;

import com.ote.github.api.component.ICommunicationService;
import com.ote.github.api.component.IRepositoryConfigurationService;
import com.ote.github.api.component.RepositoryConfiguration;
import com.ote.github.api.eventhandler.IHandler;
import com.ote.github.api.json.Issue;
import com.ote.github.api.json.PullRequestEvent;
import com.ote.github.api.json.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PullRequestLabeledHandler extends AdtPullRequestEventHandler implements IHandler<PullRequestEvent, HttpStatus> {

    @Autowired
    public PullRequestLabeledHandler(IRepositoryConfigurationService remoteConfigurationService, ICommunicationService communicationService) {
        super(remoteConfigurationService, communicationService);
    }

    @Override
    public HttpStatus handle(PullRequestEvent event) {

        String targetStatusContext = statusConfiguration.getContextDoNotMergeLabelStatus();

        RepositoryConfiguration repositoryConfiguration = getRepositoryConfiguration(event.getRepository());

        Issue issue = communicationService.get(event.getPullRequest().getIssueUrl(), Issue.class);

        if (issue.getLabels().stream().anyMatch(l -> l.getName().
                equals(repositoryConfiguration.getDoNotMergeLabelName()))) {
            Status.State targetState = Status.State.ERROR;
            return postStatus(event, targetState, targetStatusContext, repositoryConfiguration);
        } else {
            Status.State targetState = Status.State.SUCCESS;
            return postStatus(event, targetState, targetStatusContext, repositoryConfiguration);
        }
    }
}
