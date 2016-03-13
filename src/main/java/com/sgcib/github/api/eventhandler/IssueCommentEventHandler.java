package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.eventhandler.configuration.IssueCommentConfiguration;
import com.sgcib.github.api.eventhandler.configuration.RemoteConfiguration;
import com.sgcib.github.api.payloayd.IssueCommentPayload;
import com.sgcib.github.api.payloayd.PullRequest;
import com.sgcib.github.api.payloayd.Repository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class IssueCommentEventHandler extends AdtEventHandler<IssueCommentPayload> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(IssueCommentEventHandler.class);

    @Autowired
    @Lazy(false)
    private IssueCommentConfiguration issueCommentConfiguration;

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

        switch (issueCommentConfiguration.getType(comment)) {
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
                && issueCommentConfiguration.isRemoteConfigurationChecked()
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
        Status status = state.create(event.getComment().getUser().getLogin());

        return postStatus(statusesUrl, status, remoteRepositoryName);
    }

    private boolean isAutoApprovementAuthorized(Repository repository, String remoteRepositoryName) throws EventHandlerException {

        String remoteConfigurationFile = issueCommentConfiguration.getRemoteConfigurationPath();

        if (logger.isDebugEnabled()) {
            logger.debug(remoteRepositoryName + " : loading remote configuration file (" + remoteConfigurationFile + ") in order to check auto-approval");
        }

        String defaultBranch = repository.getDefaultBranch();
        String contentsUrl = repository.getContentsUrl();
        contentsUrl = contentsUrl.replace("{+path}", remoteConfigurationFile + "?ref=" + defaultBranch);

        Result result = new Result();
        getRemoteConfigurationFile(contentsUrl, remoteRepositoryName).ifPresent(p -> result.setAutoApprovalAuthorized(p.isAutoApprovalAuthorized()));

        return result.isAutoApprovalAuthorized();
    }

    @Data
    private class Result {

        private boolean isAutoApprovalAuthorized;
    }

    private Optional<RemoteConfiguration> getRemoteConfigurationFile(String url, String remoteRepositoryName) {

        try {
            File file = jsonService.parse(File.class, restTemplate.getForObject(url, String.class));
            String content = restTemplate.getForObject(file.getDownloadUrl(), String.class);
            try {
                Properties prop = new Properties();
                prop.load(new ByteArrayInputStream(content.getBytes()));
                return Optional.of(new RemoteConfiguration(prop));
            } catch (IOException e) {
                logger.error(remoteRepositoryName + " : error while parsing remote configuration content " + content, e);
                return Optional.empty();
            }
        } catch (RestClientException | IOException e) {
            logger.error(remoteRepositoryName + " : unable to retrieve remote configuration file", e);
            return Optional.empty();
        }
    }

    private PullRequest getPullRequest(String url, String remoteRepositoryName) throws EventHandlerException {

        if (logger.isDebugEnabled()) {
            logger.debug(remoteRepositoryName + " : retrieving pull request : " + url);
        }

        String pullRequest = "";
        try {

            pullRequest = restTemplate.getForObject(url, String.class);

            if (logger.isDebugEnabled()) {
                logger.debug(remoteRepositoryName + " : pull request is : " + pullRequest);
            }

            return jsonService.parse(PullRequest.class, pullRequest);

        } catch (RestClientException e) {
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST,
                    remoteRepositoryName + " : error while retrieving pull_request : " + url);
        } catch (IOException e) {
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY,
                    remoteRepositoryName + " : error while parsing pull_request result : " + url, pullRequest);
        }
    }
}
