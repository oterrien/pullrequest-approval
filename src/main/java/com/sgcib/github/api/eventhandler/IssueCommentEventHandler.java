package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.eventhandler.configuration.Configuration;
import com.sgcib.github.api.json.IssueCommentPayload;
import com.sgcib.github.api.json.PullRequest;
import com.sgcib.github.api.json.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

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

        switch (configuration.getType(comment)) {
            case APPROVEMENT:
                if (currentState != Status.State.SUCCESS) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Pull request is not yet approved -> set status to success");
                    }
                    return postStatus(event, Status.State.SUCCESS);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Pull request is currently approved -> no change");
                    }
                }
                break;
            case REJECTION:
                if (currentState != Status.State.ERROR && currentState != Status.State.FAILURE) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Pull request is not yet rejected -> set status to error");
                    }
                    return postStatus(event, Status.State.ERROR);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Pull request is currently rejected -> no change");
                    }
                }
                break;
            case PENDING:
                if (currentState != Status.State.PENDING) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Pull request is not yet pending -> set status to pending");
                    }
                    return postStatus(event, Status.State.PENDING);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Pull request is currently pending -> no change");
                    }
                }
                break;
        }

        return HttpStatus.OK;
    }

    private HttpStatus postStatus(IssueCommentPayload event, Status.State state) throws EventHandlerException {

        if (logger.isDebugEnabled())
            logger.debug("Setting pull request state to '" + state.getState() + "'");

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = getPullRequest(pullUrl); // already executed --> find a threadsafe way to reuse the previous call

        if (state == Status.State.SUCCESS
                && configuration.isRemoteConfigurationChecked()
                && event.getComment().getUser().getId() == pullRequest.getUser().getId()) {

            if (!isAutoApprovementAuthorized(event.getRepository())) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Same user cannot approve pull request");
                }
                return HttpStatus.UNAUTHORIZED;
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("OK, same user is able to approve his own pull request");
                }
            }
        }

        String statusesUrl = pullRequest.getStatusesUrl();
        Status status = state.createStatus(event.getComment().getUser().getLogin());

        return communicationService.post(statusesUrl, status);
    }

    private boolean isAutoApprovementAuthorized(Repository repository) throws EventHandlerException {

        return getRemoteConfiguration(repository).
                map(remoteConfiguration -> remoteConfiguration.isAutoApprovalAuthorized()).
                orElse(false);
    }

    private PullRequest getPullRequest(String url) throws EventHandlerException {

        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving pull request : " + url);
        }

        return communicationService.get(url, PullRequest.class);
    }
}
