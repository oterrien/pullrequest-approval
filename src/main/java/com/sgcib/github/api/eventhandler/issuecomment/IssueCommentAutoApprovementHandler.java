package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.service.*;
import com.sgcib.github.api.json.IssueCommentEvent;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.json.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class IssueCommentAutoApprovementHandler extends AdtIssueCommentEventHandler implements IHandler<IssueCommentEvent, HttpStatus> {

    @Autowired
    public IssueCommentAutoApprovementHandler(Configuration configuration, IRemoteConfigurationService remoteConfigurationService, ICommunicationService communicationService, StatusService statusService) {
        super(configuration, remoteConfigurationService, communicationService, statusService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) {

        if (isTechnicalUserAction(event)) {
            return HttpStatus.OK;
        }

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

        User user = event.getComment().getUser();
        String templateName = configuration.getAutoApprovalAlertMessageTemplateFileName();

        // TODO : to be fixed. Must be granted
        //List<User> administrators = getAdministrators(event.getRepository());

        List<User> administrators = Stream.of(configuration.getTechnicalUserLogin(), user.getLogin()).
                map(s -> {
                    User admin = new User();
                    admin.setLogin(s);
                    return admin;
                }).
                collect(Collectors.toList());

        Map<String, String> param = new HashMap<>(10);
        param.put("user", user.getLogin());
        param.put("owners", administrators.stream().map(u -> "@" + u.getLogin()).collect(Collectors.joining(", ")));
        param.put("issue.comments.list.auto_approval", configuration.getAutoApprovalCommentsList().stream().map(c -> "**" + c + "**").collect(Collectors.joining(" or ")));
        param.put("reason", event.getComment().getBody().trim());

        postComment(templateName, param, event);
    }
}