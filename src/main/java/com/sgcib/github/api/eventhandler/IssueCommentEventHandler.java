package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.payloayd.IssueCommentPayload;
import com.sgcib.github.api.payloayd.PullRequest;
import com.sgcib.github.api.payloayd.Repository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class IssueCommentEventHandler extends AdtEventHandler<IssueCommentPayload> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(IssueCommentEventHandler.class);

    public IssueCommentEventHandler() {
        super(IssueCommentPayload.class);
    }

    @Override
    public HttpStatus handle(IssueCommentPayload event) throws EventHandlerException {

        String remoteRepositoryName = event.getRepository().getName();

        if (logger.isInfoEnabled()) {
            logger.info(remoteRepositoryName + " : event received");
        }

        String comment = event.getComment().getBody().trim().toLowerCase();

        if (logger.isDebugEnabled()) {
            logger.debug(remoteRepositoryName + " : issue comment is '" + comment + "'");
        }

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = getPullRequest(pullUrl, remoteRepositoryName);
        Status.State currentState = getCurrentState(pullRequest.getStatusesUrl(), remoteRepositoryName);

        switch (configuration.getType(comment)) {
            case APPROVEMENT:
                if (currentState != Status.State.SUCCESS) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(remoteRepositoryName + " : pull request is not yet approved -> set status to success");
                    }
                    return postStatus(event, Status.State.SUCCESS, remoteRepositoryName);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(remoteRepositoryName + " : pull request is currently approved -> no change");
                    }
                }
                break;
            case REJECTION:
                if (currentState != Status.State.ERROR && currentState != Status.State.FAILURE) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(remoteRepositoryName + " : pull request is not yet rejected -> set status to error");
                    }
                    return postStatus(event, Status.State.ERROR, remoteRepositoryName);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(remoteRepositoryName + " : pull request is currently rejected -> no change");
                    }
                }
                break;
            case PENDING:
                if (currentState != Status.State.PENDING) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(remoteRepositoryName + " : pull request is not yet pending -> set status to pending");
                    }
                    return postStatus(event, Status.State.PENDING, remoteRepositoryName);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(remoteRepositoryName + " : pull request is currently pending -> no change");
                    }
                }
                break;
        }

        return HttpStatus.OK;
    }

    private HttpStatus postStatus(IssueCommentPayload event, Status.State state, String remoteRepositoryName) throws EventHandlerException {

        if (logger.isDebugEnabled())
            logger.debug(remoteRepositoryName + " : setting pull request state to '" + state.getState() + "'");

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = getPullRequest(pullUrl, remoteRepositoryName);

        if (state == Status.State.SUCCESS
                && configuration.isRemoteConfigurationChecked()
                && event.getComment().getUser().getId() == pullRequest.getUser().getId()) {

            if (!isAutoApprovementAuthorized(event.getRepository(), remoteRepositoryName)) {
                if (logger.isWarnEnabled()) {
                    logger.warn(remoteRepositoryName + " : same user cannot approve pull request");
                }
                return HttpStatus.UNAUTHORIZED;
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info(remoteRepositoryName + " : OK, same user is able to approve his own pull request");
                }
            }
        }

        String statusesUrl = pullRequest.getStatusesUrl();
        Status status = state.createStatus(event.getComment().getUser().getLogin());

        return communicationService.post(statusesUrl, status, remoteRepositoryName);
    }

    private boolean isAutoApprovementAuthorized(Repository repository, String remoteRepositoryName) throws EventHandlerException {

        Result result = new Result();
        getRemoteConfiguration(repository).ifPresent(p -> result.setAutoApprovalAuthorized(p.isAutoApprovalAuthorized()));

        return result.isAutoApprovalAuthorized();
    }

    @Data
    private class Result {

        private boolean isAutoApprovalAuthorized;
    }

    private PullRequest getPullRequest(String url, String remoteRepositoryName) throws EventHandlerException {

        if (logger.isDebugEnabled()) {
            logger.debug(remoteRepositoryName + " : retrieving pull request : " + url);
        }

        return communicationService.get(url, remoteRepositoryName, PullRequest.class);
    }
}
