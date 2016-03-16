package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.eventhandler.configuration.Configuration;
import com.sgcib.github.api.eventhandler.configuration.RemoteConfiguration;
import com.sgcib.github.api.json.*;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class IssueCommentEventHandler extends AdtEventHandler<IssueCommentPayload> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(IssueCommentEventHandler.class);

    @Autowired
    public IssueCommentEventHandler(Configuration configuration, ICommunicationService communicationService) {
        super(IssueCommentPayload.class, configuration, communicationService);
    }

    @Override
    public HttpStatus handle(IssueCommentPayload event) throws EventHandlerException {

        if (logger.isInfoEnabled()) {
            logger.info("Event received");
        }

        String comment = event.getComment().getBody().trim().toLowerCase();

        if (logger.isDebugEnabled()) {
            logger.debug("Issue comment is '" + comment + "'");
        }

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = getPullRequest(pullUrl);
        Status.State currentState = getCurrentState(pullRequest.getStatusesUrl());
        Configuration.Type type = configuration.getType(comment);

        switch (type) {
            case APPROVEMENT:
                if (currentState != Status.State.SUCCESS) {
                    logTarget(type, Status.State.SUCCESS);
                    PostStatusProcessor processor = new PostStatusProcessor() {
                        @Override
                        public HttpStatus process() throws EventHandlerException {
                            if (configuration.isRemoteConfigurationChecked() &&
                                    event.getComment().getUser().getId() == pullRequest.getUser().getId()) {

                                if (!isAutoApprovementAuthorized(remoteConfiguration)) {
                                    if (logger.isWarnEnabled()) {
                                        logger.warn("Same user cannot approve pull request");
                                    }
                                    postAutoApprovalAlertMessage(pullRequest, event.getIssue().getUser());
                                    return HttpStatus.UNAUTHORIZED;
                                } else {
                                    if (logger.isInfoEnabled()) {
                                        logger.info("OK, same user is able to approve his own pull request");
                                    }
                                }
                            }
                            return super.process();
                        }
                    };

                    return processor.
                            repository(event.getRepository()).
                            targetState(Status.State.SUCCESS).
                            user(event.getComment().getUser()).
                            pullRequest(pullRequest).
                            process();
                } else {
                    logNoChange(type);
                }
                break;
            case REJECTION:
                if (currentState != Status.State.ERROR && currentState != Status.State.FAILURE) {
                    logTarget(type, Status.State.ERROR);
                    return new PostStatusProcessor().
                            repository(event.getRepository()).
                            targetState(Status.State.ERROR).
                            user(event.getComment().getUser()).
                            pullRequest(pullRequest).
                            process();
                } else {
                    logNoChange(type);
                }
                break;
            case PENDING:
                if (currentState != Status.State.PENDING) {
                    logTarget(type, Status.State.PENDING);
                    return new PostStatusProcessor().
                            repository(event.getRepository()).
                            targetState(Status.State.PENDING).
                            user(event.getComment().getUser()).
                            pullRequest(pullRequest).
                            process();
                } else {
                    logNoChange(type);
                }
                break;
            case AUTO_APPROVEMENT:
                if (currentState != Status.State.SUCCESS) {
                    logTarget(type, Status.State.SUCCESS);

                    PostStatusProcessor processor = new PostStatusProcessor() {
                        @Override
                        public HttpStatus process() throws EventHandlerException {

                            // TODO Find owner list of repository
                            // TODO Post a message with @${owner} to alert ${user} has approved its own pull request for the given reason
                            // `${reason}`
                            return super.process();
                        }
                    };

                    return processor.
                            repository(event.getRepository()).
                            targetState(Status.State.SUCCESS).
                            user(event.getComment().getUser()).
                            pullRequest(pullRequest).
                            process();
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

        protected Optional<RemoteConfiguration> remoteConfiguration;

        public PostStatusProcessor repository(Repository repository) {
            this.repository = repository;
            this.remoteConfiguration = getRemoteConfiguration(repository);
            return this;
        }

        public PostStatusProcessor targetState(Status.State targetState) {
            this.targetState = targetState;
            return this;
        }

        public PostStatusProcessor user(User user) {
            this.user = user;
            return this;
        }

        public PostStatusProcessor pullRequest(PullRequest pullRequest) {
            this.pullRequest = pullRequest;
            return this;
        }

        public HttpStatus process() throws EventHandlerException {
            String statusesUrl = pullRequest.getStatusesUrl();
            Status targetStatus = new Status(targetState, user.getLogin(), configuration, remoteConfiguration);
            return communicationService.post(statusesUrl, targetStatus);
        }
    }

    private HttpStatus tryPostStatus(IssueCommentPayload event, Status.State state) throws EventHandlerException {

        if (logger.isDebugEnabled())
            logger.debug("Setting pull request state to '" + state.getValue() + "'");

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = getPullRequest(pullUrl); // already executed --> find a threadsafe way to reuse the previous call
        Optional<RemoteConfiguration> remoteConfiguration = getRemoteConfiguration(event.getRepository());

        if (state == Status.State.SUCCESS && configuration.isRemoteConfigurationChecked() &&
                event.getComment().getUser().getId() == pullRequest.getUser().getId()) {

            if (!isAutoApprovementAuthorized(remoteConfiguration)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Same user cannot approve pull request");
                }

                postAutoApprovalAlertMessage(pullRequest, event.getIssue().getUser());

                return HttpStatus.UNAUTHORIZED;
            }

            if (logger.isInfoEnabled()) {
                logger.info("OK, same user is able to approve his own pull request");
            }
        }

        String statusesUrl = pullRequest.getStatusesUrl();
        Status status = new Status(state, event.getComment().getUser().getLogin(), configuration, remoteConfiguration);

        return communicationService.post(statusesUrl, status);
    }

    private void postAutoApprovalAlertMessage(PullRequest pullRequest, User user) throws EventHandlerException {

        String commentsUrl = pullRequest.getCommentsUrl();

        Map<String, String> param = new HashMap<>(10);
        param.put("user", "@" + user.getLogin());
        param.put("issue.comments.list.auto_approval", configuration.getAutoApprovalCommentsList().stream().collect(Collectors.joining(" or ")));
        param.put("issue.comments.list.auto_approval.one", configuration.getAutoApprovalCommentsList().get(0));

        Comment comment = new Comment();
        comment.setBody(StrSubstitutor.replace(configuration.getAutoApprovalAlertMessageTemplateByDefault(), param));

        communicationService.post(commentsUrl, comment);
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
