package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.FilesUtils;
import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.component.ICommunicationService;
import com.sgcib.github.api.component.IRepositoryConfigurationService;
import com.sgcib.github.api.eventhandler.AdtEventHandler;
import com.sgcib.github.api.eventhandler.EventHandlerException;
import com.sgcib.github.api.eventhandler.IHandler;
import com.sgcib.github.api.json.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AdtIssueCommentEventHandler extends AdtEventHandler<IssueCommentEvent> implements IHandler<IssueCommentEvent, HttpStatus> {

    protected AdtIssueCommentEventHandler(IRepositoryConfigurationService remoteConfigurationService, ICommunicationService communicationService) {
        super(remoteConfigurationService, communicationService);
    }

    @Override
    protected PullRequest getPullRequest(IssueCommentEvent event) {
        return event.getIssue().getPullRequest();
    }

    @Override
    protected Repository getRepository(IssueCommentEvent event) {
        return event.getRepository();
    }

    protected void enrich(IssueCommentEvent event) {

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = communicationService.get(pullUrl, PullRequest.class);
        event.getIssue().setPullRequest(pullRequest);
    }

    protected void postComment(String templateName, Map<String, String> param, IssueCommentEvent event) {
        try {
            String comment = FilesUtils.readFileInClasspath(templateName, param);
            Comment alertMessage = new Comment();
            alertMessage.setBody(comment);
            communicationService.post(event.getIssue().getPullRequest().getCommentsUrl(), alertMessage);
        } catch (Exception e) {
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while posting comment for repository '" + event.getRepository().getName() + "'");
        }
    }

    protected boolean isUserAuthorized(Repository repository, User user) {

        Optional<List<User>> authorizedUsers = getAuthorizedUsers(repository);

        if (!authorizedUsers.isPresent()){
            return false;
        }

        return authorizedUsers.get().
                stream().
                filter(usr -> usr.getLogin().equals(user.getLogin())).
                findAny().
                isPresent();
    }

    private Optional<List<User>> getAuthorizedUsers(Repository repository) {

        String collaboratorsUrl = repository.getCollaboratorsUrl();
        try {
            collaboratorsUrl = collaboratorsUrl.replace("{/collaborator}", "");

            String str = communicationService.get(collaboratorsUrl);
            str = "{\"users\":" + str + "}";
            Users users = JsonUtils.parse(str, Users.class);
            return Optional.of(users.getUsers());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to retrieve collaborators from " + collaboratorsUrl, e);
            }
            return Optional.empty();
        }
    }
}
