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

        logger.info("Event received from repository '" + event.getRepository().getName() + "'");

        String comment = event.getComment().getBody().trim().toLowerCase();

        if (logger.isDebugEnabled()) {
            logger.debug("Issue comment is '" + comment + "'");
        }

        switch (issueCommentConfiguration.getType(comment)) {
            case APPROVEMENT:
                return postStatus(event, Status.State.SUCCESS);
            case REJECTION:
                return postStatus(event, Status.State.ERROR);
            case PENDING:
                return postStatus(event, Status.State.PENDING);
        }

        return HttpStatus.OK;
    }

    private HttpStatus postStatus(IssueCommentPayload event, Status.State state) throws EventHandlerException {

        if (logger.isDebugEnabled())
            logger.debug("Trying to set pull request's state to '" + state.getState() + "'");

        String pullUrl = event.getIssue().getPullRequest().getUrl();
        PullRequest pullRequest = getPullRequest(pullUrl);

        if (state == Status.State.SUCCESS
                && issueCommentConfiguration.isRemoteConfigurationChecked()
                && event.getComment().getUser().getId() == pullRequest.getUser().getId()) {

            if (!isAutoApprovementAuthorized(event.getRepository())) {
                if (logger.isInfoEnabled())
                    logger.info("Same user cannot approve pull request");

                return HttpStatus.UNAUTHORIZED;

            } else {
                if (logger.isInfoEnabled())
                    logger.info("Same user is able to approve his own pull request");
            }
        }

        String statusesUrl = pullRequest.getStatusesUrl();
        Status status = generateStatus(state, Optional.of(event.getComment().getUser().getLogin()));

        return postStatus(statusesUrl, status);
    }

    private boolean isAutoApprovementAuthorized(Repository repository) throws EventHandlerException {

        String remoteConfigurationFile = issueCommentConfiguration.getRemoteConfigurationPath();

        if (logger.isDebugEnabled())
            logger.debug("Load remote configuration file (" + remoteConfigurationFile + ") in order to check auto-approval");

        String defaultBranch = repository.getDefaultBranch();
        String contentsUrl = repository.getContentsUrl();
        contentsUrl = contentsUrl.replace("{+path}", remoteConfigurationFile + "?ref=" + defaultBranch);

        Result result = new Result();
        getRemoteConfigurationFile(contentsUrl).ifPresent(p -> result.setAutoApprovalAuthorized(p.isAutoApprovalAuthorized()));

        return result.isAutoApprovalAuthorized();
    }

    @Data
    private class Result {

        private boolean isAutoApprovalAuthorized;
    }

    private Optional<RemoteConfiguration> getRemoteConfigurationFile(String url) {
        String fileAsString = "";
        try {
            fileAsString = restTemplate.getForObject(url, String.class);

            if (logger.isDebugEnabled())
                logger.debug("File is : " + fileAsString);

            File file = jsonService.parse(File.class, fileAsString);

            String content = restTemplate.getForObject(file.getDownloadUrl(), String.class);
            try {
                Properties prop = new Properties();
                prop.load(new ByteArrayInputStream(content.getBytes()));
                return Optional.of(new RemoteConfiguration(prop));
            } catch (IOException e) {
                logger.error("Error while parsing remote configuration content " + content, e);
                return Optional.empty();
            }
        } catch (RestClientException | IOException e) {
            logger.error("Unable to retrieve remote configuration file", e);
            return Optional.empty();
        }
    }

    private PullRequest getPullRequest(String url) throws EventHandlerException {

        if (logger.isDebugEnabled())
            logger.debug("Retrieving pull request : " + url);

        String pullRequest = "";
        try {

            pullRequest = restTemplate.getForObject(url, String.class);

            if (logger.isDebugEnabled())
                logger.debug("PullRequest is : " + pullRequest);

            return jsonService.parse(PullRequest.class, pullRequest);

        } catch (RestClientException e) {
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST, "Error while retrieving pull_request : " + url);
        } catch (IOException e) {
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while parsing pull_request result : " + url, pullRequest);
        }
    }
}
