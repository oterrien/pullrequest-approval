package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.FilesUtils;
import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.configuration.RemoteConfiguration;
import com.sgcib.github.api.eventhandler.EventHandlerException;
import com.sgcib.github.api.json.*;
import com.sgcib.github.api.service.ICommunicationService;
import com.sgcib.github.api.service.RemoteConfigurationException;
import com.sgcib.github.api.service.RemoteConfigurationService;
import com.sgcib.github.api.service.StatusService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AdtIssueCommentEventHandler implements IHandler<IssueCommentEvent, HttpStatus> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ICommunicationService communicationService;

    protected final StatusService statusService;

    protected final RemoteConfigurationService remoteConfigurationService;

    protected final Configuration configuration;

    protected AdtIssueCommentEventHandler(Configuration configuration, RemoteConfigurationService remoteConfigurationService, ICommunicationService communicationService, StatusService statusService) {

        this.configuration = configuration;
        this.remoteConfigurationService = remoteConfigurationService;
        this.communicationService = communicationService;
        this.statusService = statusService;
    }

    protected void enrich(IssueCommentEvent event) {

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = communicationService.get(pullUrl, PullRequest.class);
        event.getIssue().setPullRequest(pullRequest);
    }

    protected boolean isStateAlreadySet(IssueCommentEvent event, Status.State targetState, String targetStatusContext) {

        Status.State currentState = statusService.getCurrentState(event.getIssue().getPullRequest().getStatusesUrl(), targetStatusContext);
        if (currentState == targetState) {
            if (logger.isDebugEnabled()) {
                logger.debug("Status '" + targetStatusContext + "' for repository '" + event.getRepository().getName() + "' is currently set to " + targetState.getValue() + " -> no change");
            }
            return true;
        }
        return false;
    }

    // TODO change with configuration
    protected List<User> getAdministrators(Repository repository) {

        try {
            String collaboratorsUrl = repository.getCollaboratorsUrl().replace("{/collaborator}", StringUtils.EMPTY);

            String str = communicationService.get(collaboratorsUrl);
            str = "{\"users\":" + str + "}";

            Users users = JsonUtils.parse(str, Users.class);
            return users.getUsers().stream().filter(user -> user.getPermissions().isAdmin()).collect(Collectors.toList());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to retrieve administrators of repository '" + repository.getName() + "'", e);
            }
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Unable to retrieve administrators of repository");
        }
    }

    protected HttpStatus postStatus(IssueCommentEvent event, Status.State targetState, String targetStatusContext) {

        if (logger.isDebugEnabled()) {
            logger.debug("Status '" + targetStatusContext + "' for repository '" + event.getRepository().getName() + "' will be updated to " + targetState);
        }

        try {
            RemoteConfiguration remoteConfiguration = remoteConfigurationService.createRemoteConfiguration(event.getRepository());
            PullRequest pullRequest = event.getIssue().getPullRequest();
            Status targetStatus = statusService.createStatus(targetState, pullRequest.getUser().getLogin(), targetStatusContext, remoteConfiguration);
            return communicationService.post(pullRequest.getStatusesUrl(), targetStatus);
        } catch (RemoteConfigurationException e) {
            throw new EventHandlerException(e, HttpStatus.PRECONDITION_FAILED, e.getMessage());
        }
    }

    protected void postComment(String templateName, Map<String, String> param, String commentUrl){
        try {
            Comment alertMessage = new Comment();
            alertMessage.setBody(FilesUtils.readFileInClasspath(templateName, param));
            communicationService.post(commentUrl, alertMessage);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to read template : " + templateName, e);
            }
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while reading template from " + templateName);
        }
    }
}
