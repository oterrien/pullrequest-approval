package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.component.ICommunicationService;
import com.sgcib.github.api.component.IRepositoryConfigurationService;
import com.sgcib.github.api.component.RepositoryConfiguration;
import com.sgcib.github.api.eventhandler.IHandler;
import com.sgcib.github.api.json.IssueCommentEvent;
import com.sgcib.github.api.json.Repository;
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
    public IssueCommentAutoApprovementHandler(IRepositoryConfigurationService remoteConfigurationService, ICommunicationService communicationService) {
        super(remoteConfigurationService, communicationService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) {

        if (!isUserAuthorized(event.getRepository(), event.getComment().getUser())){
            if (logger.isDebugEnabled()) {
                logger.debug("User "+ event.getComment().getUser().getLogin() +" is not authorized to approve pull request for repository '" + event.getRepository().getName() + "'");
            }
            return HttpStatus.UNAUTHORIZED;
        }

        enrich(event);

        String targetStatusContext = statusConfiguration.getContextPullRequestApprovalStatus();
        Status.State targetState = Status.State.SUCCESS;

        postAutoApprovalAlertMessage(event);

        return postStatus(event, targetState, targetStatusContext);

    }

    private void postAutoApprovalAlertMessage(IssueCommentEvent event) {

        User user = event.getComment().getUser();
        String templateName = issueCommentConfiguration.getAutoApprovalAlertMessageTemplateFileName();

        List<String> administrators = getAdministrators(event.getRepository());

        Map<String, String> param = new HashMap<>(10);
        param.put("user", user.getLogin());
        param.put("owners", administrators.stream().map(admin -> "@" + admin).collect(Collectors.joining(", ")));
        param.put("issue.comments.list.auto_approval", issueCommentConfiguration.getAutoApprovalCommentsList().stream().map(c -> "**" + c + "**").collect(Collectors.joining(" or ")));
        param.put("reason", event.getComment().getBody().trim());

        postComment(templateName, param, event);
    }

    private List<String> getAdministrators(Repository repository){

        User owner = repository.getOwner();
        User.Type type = User.Type.of(owner.getType());

        if (type == User.Type.ORGANIZATION){
            try {
                RepositoryConfiguration repositoryConfiguration = repositoryConfigurationService.createRemoteConfiguration(repository);
                return repositoryConfiguration.getAdminTeam();
            } catch (Exception e){
                if (logger.isWarnEnabled()) {
                    logger.warn("unable to retrieve remote configuration for repository '" + repository.getName() +"'", logger.isDebugEnabled() ? e : e.getMessage());
                }
            }
        }

        return Stream.of(owner.getLogin()).collect(Collectors.toList());

    }
}