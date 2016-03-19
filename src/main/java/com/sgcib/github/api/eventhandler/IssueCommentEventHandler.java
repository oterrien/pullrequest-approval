package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.FilesUtils;
import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.configuration.RemoteConfiguration;
import com.sgcib.github.api.json.*;
import com.sgcib.github.api.service.ICommunicationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IssueCommentEventHandler extends AdtEventHandler<IssueCommentEvent> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(IssueCommentEventHandler.class);

    @Autowired
    public IssueCommentEventHandler(Configuration configuration, ICommunicationService communicationService) {
        super(IssueCommentEvent.class, configuration, communicationService);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) throws EventHandlerException {

        String comment = event.getComment().getBody().trim();

        if (logger.isDebugEnabled()) {
            logger.debug("Issue comments '" + comment + "'");
        }

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = getPullRequest(pullUrl);
        Status.State currentState = getCurrentState(pullRequest.getStatusesUrl());
        Configuration.Type type = configuration.getType(comment);

        switch (type) {
            case APPROVEMENT:
                if (currentState != Status.State.SUCCESS) {
                    logTarget(type, Status.State.SUCCESS);
                    PostStatusProcessor processor = new PostStatusProcessor(event.getRepository(), pullRequest, Status.State.SUCCESS, event.getComment().getUser(), comment) {
                        @Override
                        public HttpStatus process() throws EventHandlerException {

                            // TODO : change it. If remote config is not checked, auto-approvement.authorization is determined by default
                            // TODO : remote isRemoteConfigurationChecked : check by default. If not found, use default value stored in a local configuration.properties
                            if (configuration.isRemoteConfigurationChecked() &&
                                    event.getComment().getUser().getId() == pullRequest.getUser().getId()) {

                                if (!isAutoApprovementAuthorized(remoteConfiguration)) {
                                    if (logger.isWarnEnabled()) {
                                        logger.warn("Same user cannot approve pull request");
                                    }
                                    postAutoApprovalAdviceMessage(pullRequest, user);
                                    return HttpStatus.PRECONDITION_REQUIRED;
                                } else {
                                    if (logger.isInfoEnabled()) {
                                        logger.info("OK, same user is able to approve his own pull request");
                                    }
                                }
                            }
                            return super.process();
                        }
                    };

                    return processor.process();
                } else {
                    logNoChange(type);
                }
                break;
            case REJECTION:
                if (currentState != Status.State.ERROR && currentState != Status.State.FAILURE) {
                    logTarget(type, Status.State.ERROR);
                    return new PostStatusProcessor(event.getRepository(), pullRequest, Status.State.ERROR, event.getComment().getUser(), comment).
                            process();
                } else {
                    logNoChange(type);
                }
                break;
            case PENDING:
                if (currentState != Status.State.PENDING) {
                    logTarget(type, Status.State.PENDING);
                    return new PostStatusProcessor(event.getRepository(), pullRequest, Status.State.PENDING, event.getComment().getUser(), comment).
                            process();
                } else {
                    logNoChange(type);
                }
                break;
            case AUTO_APPROVEMENT:
                if (currentState != Status.State.SUCCESS) {
                    logTarget(type, Status.State.SUCCESS);

                    PostStatusProcessor processor = new PostStatusProcessor(event.getRepository(), pullRequest, Status.State.SUCCESS, event.getComment().getUser(), comment) {
                        @Override
                        public HttpStatus process() throws EventHandlerException {

                            postAutoApprovalAlertMessage(repository, pullRequest, user, comment);
                            return super.process();
                        }
                    };

                    return processor.process();
                } else {
                    logNoChange(type);
                }
                break;
        }

        return HttpStatus.OK;
    }

    private void logNoChange(Configuration.Type type) {
        if (logger.isDebugEnabled()) {
            logger.debug("Pull request is currently " + type.getValue() + " -> no change");
        }
    }

    private void logTarget(Configuration.Type type, Status.State targetState) {
        if (logger.isDebugEnabled()) {
            logger.debug("Pull request is not yet " + type.getValue() + " -> trying to set status to " + targetState.getValue());
        }
    }

    private class PostStatusProcessor {

        protected Repository repository;

        protected PullRequest pullRequest;

        protected Status.State targetState;

        protected User user;

        protected String comment;

        protected Optional<RemoteConfiguration> remoteConfiguration;

        public PostStatusProcessor(Repository repository, PullRequest pullRequest, Status.State targetState, User user, String comment) {
            this.repository = repository;
            this.pullRequest = pullRequest;
            this.targetState = targetState;
            this.user = user;
            this.comment = comment;
            this.remoteConfiguration = getRemoteConfiguration(repository);
        }

        public HttpStatus process() throws EventHandlerException {

            if (logger.isDebugEnabled()) {
                logger.debug("Setting pull request state to '" + targetState.getValue() + "'");
            }

            String statusesUrl = pullRequest.getStatusesUrl();
            Status targetStatus = new Status(targetState, user.getLogin(), configuration, remoteConfiguration);
            return communicationService.post(statusesUrl, targetStatus);
        }
    }

    private void postAutoApprovalAdviceMessage(PullRequest pullRequest, User user) throws EventHandlerException {

        String commentsUrl = pullRequest.getCommentsUrl();

        try {
            Map<String, String> param = new HashMap<>(10);
            param.put("user", "@" + user.getLogin());
            param.put("issue.comments.list.auto_approval", configuration.getAutoApprovalCommentsList().stream().collect(Collectors.joining(" or ")));

            Comment comment = new Comment();
            comment.setBody(FilesUtils.readFileInClasspath(configuration.getAutoApprovalAdviceMessageTemplateFileName(), param));
            communicationService.post(commentsUrl, comment);
        } catch (Exception e) {
            String templateName = configuration.getAutoApprovalAdviceMessageTemplateFileName();
            if (logger.isErrorEnabled()) {
                logger.error("Unable to read template : " + templateName, e);
            }
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while reading template from " + templateName);
        }
    }

    private void postAutoApprovalAlertMessage(Repository repository, PullRequest pullRequest, User user, String reason) throws EventHandlerException {

        // TODO move pullRequest interaction to a PullRequestService

        String commentsUrl = pullRequest.getCommentsUrl();

        try {
            List<User> administrators = getAdministrators(repository);

            Map<String, String> param = new HashMap<>(10);
            param.put("user", user.getLogin());
            param.put("owners", administrators.stream().map(u -> "@" + u.getLogin()).collect(Collectors.joining(", ")));
            param.put("issue.comments.list.auto_approval", configuration.getAutoApprovalCommentsList().stream().collect(Collectors.joining(" or ")));
            param.put("reason", reason);


            Comment comment = new Comment();
            comment.setBody(FilesUtils.readFileInClasspath(configuration.getAutoApprovalAlertMessageTemplateFileName(), param));
            communicationService.post(commentsUrl, comment);
        } catch (Exception e) {
            String templateName = configuration.getAutoApprovalAlertMessageTemplateFileName();
            if (logger.isErrorEnabled()) {
                logger.error("Unable to read template : " + templateName, e);
            }
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while reading template from " + templateName);
        }
    }

    private List<User> getAdministrators(Repository repository) throws EventHandlerException {

        // TODO move pullRequest interaction to a PullRequestService

        try {
            String collaboratorsUrl = repository.getCollaboratorsUrl().replace("{/collaborator}", StringUtils.EMPTY);

            String str = communicationService.get(collaboratorsUrl);
            str = "{\"users\":" + str + "}";

            Users users = JsonUtils.parse(str, Users.class);
            return users.getUsers().stream().filter(user -> user.getPermissions().isAdmin()).collect(Collectors.toList());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to retrieve administrators of repository", e);
            }
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Unable to retrieve administrators of repository");
        }
    }

    private boolean isAutoApprovementAuthorized(Optional<RemoteConfiguration> remoteConfiguration) throws EventHandlerException {

        return remoteConfiguration.
                map(conf -> conf.isAutoApprovalAuthorized()).
                orElse(configuration.isAutoApprovalAuthorizedByDefault());
    }

    private PullRequest getPullRequest(String url) throws EventHandlerException {

        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving pull request : " + url);
        }

        return communicationService.get(url, PullRequest.class);
    }
}
