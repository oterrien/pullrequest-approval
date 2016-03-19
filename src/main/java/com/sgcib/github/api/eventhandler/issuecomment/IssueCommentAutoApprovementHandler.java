package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.FilesUtils;
import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.eventhandler.EventHandlerException;
import com.sgcib.github.api.json.*;
import com.sgcib.github.api.service.ICommunicationService;
import com.sgcib.github.api.service.RemoteConfigurationService;
import com.sgcib.github.api.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class IssueCommentAutoApprovementHandler extends AdtIssueCommentEventHandler implements IHandler<IssueCommentEvent, HttpStatus> {

    @Autowired
    public IssueCommentAutoApprovementHandler(Configuration configuration, RemoteConfigurationService remoteConfigurationService, ICommunicationService communicationService, StatusService statusService) {
        super(configuration, remoteConfigurationService, communicationService, statusService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) {

        enrich(event);

        String targetStatusContext = configuration.getPullRequestApprovalStatusContext();
        Status.State targetState = Status.State.SUCCESS;

        if (isStateAlreadySet(event, targetState, targetStatusContext)) {
            return HttpStatus.OK;
        }

        postAutoApprovalAlertMessage(event);

        return postStatus(event, targetState, targetStatusContext);

    }

    private void postAutoApprovalAlertMessage(IssueCommentEvent event) {

        Repository repository = event.getRepository();
        PullRequest pullRequest = event.getIssue().getPullRequest();
        User user = event.getComment().getUser();
        String templateName = configuration.getAutoApprovalAlertMessageTemplateFileName();
        List<User> administrators = getAdministrators(repository);

        Map<String, String> param = new HashMap<>(10);
        param.put("user", user.getLogin());
        param.put("owners", administrators.stream().map(u -> "@" + u.getLogin()).collect(Collectors.joining(", ")));
        param.put("issue.comments.list.auto_approval", configuration.getAutoApprovalCommentsList().stream().collect(Collectors.joining(" or ")));
        param.put("reason", event.getComment().getBody().trim());

        postComment(templateName, param, pullRequest.getCommentsUrl());
    }
}