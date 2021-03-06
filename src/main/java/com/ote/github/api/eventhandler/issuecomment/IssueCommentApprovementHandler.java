package com.ote.github.api.eventhandler.issuecomment;

import com.ote.github.api.component.ICommunicationService;
import com.ote.github.api.component.IRepositoryConfigurationService;
import com.ote.github.api.component.RepositoryConfiguration;
import com.ote.github.api.eventhandler.IHandler;
import com.ote.github.api.json.IssueCommentEvent;
import com.ote.github.api.json.PullRequest;
import com.ote.github.api.json.Status;
import com.ote.github.api.json.User;
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
    public IssueCommentApprovementHandler(IRepositoryConfigurationService remoteConfigurationService, ICommunicationService communicationService) {
        super(remoteConfigurationService, communicationService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) {

        if (!isUserAuthorized(event.getRepository(), event.getComment().getUser())) {
            if (logger.isDebugEnabled()) {
                logger.debug("User " + event.getComment().getUser().getLogin() + " is not authorized to approve pull request for repository '" + event.getRepository().getName() + "'");
            }
            return HttpStatus.UNAUTHORIZED;
        }

        enrich(event);

        String targetStatusContext = statusConfiguration.getContextPullRequestApprovalStatus();
        Status.State targetState = Status.State.SUCCESS;

        RepositoryConfiguration repositoryConfiguration = getRepositoryConfiguration(event.getRepository());

        PullRequest pullRequest = event.getIssue().getPullRequest();
        if (Objects.equals(event.getComment().getUser().getLogin(), pullRequest.getUser().getLogin())) {


            if (!repositoryConfiguration.isAutoApprovalAuthorized()) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Same user cannot approve his own pull request for repository '" + event.getRepository().getName() + "'");
                }
                postAutoApprovalAdviceMessage(event);
                return HttpStatus.PRECONDITION_REQUIRED;
            }

            if (logger.isInfoEnabled()) {
                logger.info("Same user is authorized to approve his own pull request for repository '" + event.getRepository().getName() + "'");
            }
        }

        return postStatus(event, targetState, targetStatusContext, repositoryConfiguration);
    }

    private void postAutoApprovalAdviceMessage(IssueCommentEvent event) {

        User user = event.getComment().getUser();
        String templateName = issueCommentConfiguration.getAutoApprovalAdviceMessageTemplateFileName();

        Map<String, String> param = new HashMap<>(10);
        param.put("user", "@" + user.getLogin());
        param.put("issue.comments.list.auto_approval", issueCommentConfiguration.getAutoApprovalCommentsList().stream().map(p -> "\"**" + p + "** {reason}\"").collect(Collectors.joining(" or ")));

        postComment(templateName, param, event);
    }
}
