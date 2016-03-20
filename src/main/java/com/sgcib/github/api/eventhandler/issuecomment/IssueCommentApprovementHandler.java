package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.configuration.RemoteConfiguration;
import com.sgcib.github.api.eventhandler.EventHandlerException;
import com.sgcib.github.api.json.IssueCommentEvent;
import com.sgcib.github.api.json.PullRequest;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.json.User;
import com.sgcib.github.api.service.ICommunicationService;
import com.sgcib.github.api.service.RemoteConfigurationException;
import com.sgcib.github.api.service.RemoteConfigurationService;
import com.sgcib.github.api.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class IssueCommentApprovementHandler extends AdtIssueCommentEventHandler implements IHandler<IssueCommentEvent, HttpStatus> {

    @Autowired
    public IssueCommentApprovementHandler(Configuration configuration, RemoteConfigurationService remoteConfigurationService, ICommunicationService communicationService, StatusService statusService) {
        super(configuration, remoteConfigurationService, communicationService, statusService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) {

        if (isTechnicalUserAction(event)){
            return HttpStatus.OK;
        }

        enrich(event);

        String targetStatusContext = configuration.getPullRequestApprovalStatusContext();
        Status.State targetState = Status.State.SUCCESS;

        if (isStateAlreadySet(event, targetState, targetStatusContext)) {
            return HttpStatus.OK;
        }

        PullRequest pullRequest = event.getIssue().getPullRequest();
        if (Objects.equals(event.getComment().getUser().getLogin(), pullRequest.getUser().getLogin())) {
            try {
                RemoteConfiguration remoteConfiguration = remoteConfigurationService.createRemoteConfiguration(event.getRepository());

                if (!remoteConfiguration.isAutoApprovalAuthorized()) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Same user cannot approve his own pull request for repository '" + event.getRepository().getName() + "'");
                    }
                    postAutoApprovalAdviceMessage(event);
                    return HttpStatus.PRECONDITION_REQUIRED;
                }

                if (logger.isInfoEnabled()) {
                    logger.info("Same user is authorized to approve his own pull request for repository '" + event.getRepository().getName() + "'");
                }
            } catch (RemoteConfigurationException e) {
                throw new EventHandlerException(e, HttpStatus.PRECONDITION_FAILED, e.getMessage());
            }
        }

        return postStatus(event, targetState, targetStatusContext);
    }

    private void postAutoApprovalAdviceMessage(IssueCommentEvent event) {

        User user = event.getComment().getUser();
        String templateName = configuration.getAutoApprovalAdviceMessageTemplateFileName();

        Map<String, String> param = new HashMap<>(10);
        param.put("user", "@" + user.getLogin());
        param.put("issue.comments.list.auto_approval", configuration.getAutoApprovalCommentsList().stream().collect(Collectors.joining(" or ")));

        postComment(templateName, param, event);
    }
}
