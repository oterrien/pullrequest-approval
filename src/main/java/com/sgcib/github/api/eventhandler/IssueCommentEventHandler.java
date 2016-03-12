package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.eventhandler.configuration.IssueCommentConfiguration;
import com.sgcib.github.api.payloayd.IssueCommentPayload;
import com.sgcib.github.api.payloayd.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

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

        String statusesUrl = pullRequest.getStatusesUrl();
        Status status = generateStatus(state, Optional.of(event.getComment().getUser().getLogin()));

        return postStatus(statusesUrl, status);
    }

    private PullRequest getPullRequest(String url) throws EventHandlerException {

        if (logger.isDebugEnabled())
            logger.debug("Retrieving pull request : " + url);

        RestTemplate restTemplate = new RestTemplate();
        String pullRequest = "";
        try {
            pullRequest = restTemplate.getForObject(url, String.class);

            if (logger.isDebugEnabled())
                logger.debug("PullRequest is : " + pullRequest);

            return jsonService.parse(PullRequest.class, pullRequest);
        } catch (RestClientException e) {
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST, "Error while retrieving pull_request : " + url);
        } catch (IOException e) {
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while parsing result : " + url, pullRequest);
        }
    }



/*    enum IssueCommentType {

        APPROVEMENT(new Configuration().Approval),
        REJECTION(new Configuration().Rejection),
        ASKING_REVIEW(new Configuration().Pending);

        private List<String> comments = new ArrayList<>(10);

        IssueCommentType(String comments) {
            this.comments = Arrays.asList(comments.split(","));
        }

        public List<String> getList(){
            return  comments;
        }

    }*/

}
