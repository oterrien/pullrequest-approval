package com.sgcib.github.api.eventhandler.issuecomment;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.eventhandler.AdtEventHandlerDispatcher;
import com.sgcib.github.api.eventhandler.EventHandlerException;
import com.sgcib.github.api.json.IssueCommentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class IssueCommentEventHandlerDispatcher extends AdtEventHandlerDispatcher<IssueCommentEvent> implements IHandler<String, HttpStatus> {

    @Autowired
    private IHandler<IssueCommentEvent, HttpStatus> issueCommentApprovementHandler;

    @Autowired
    private IHandler<IssueCommentEvent, HttpStatus> issueCommentRejectionHandler;

    @Autowired
    private IHandler<IssueCommentEvent, HttpStatus> issueCommentPendingHandler;

    @Autowired
    private IHandler<IssueCommentEvent, HttpStatus> issueCommentAutoApprovementHandler;

    @Autowired
    public IssueCommentEventHandlerDispatcher(Configuration configuration) {
        super(IssueCommentEvent.class, configuration);
    }

    @Override
    public HttpStatus handle(IssueCommentEvent event) throws EventHandlerException {

        String comment = event.getComment().getBody().trim();

        if (logger.isDebugEnabled()) {
            logger.debug("Issue comments is '" + comment + "'");
        }

        switch (configuration.getType(comment)) {
            case APPROVEMENT:
                return issueCommentApprovementHandler.handle(event);
            case REJECTION:
                return issueCommentRejectionHandler.handle(event);
            case PENDING:
                return issueCommentPendingHandler.handle(event);
            case AUTO_APPROVEMENT:
                return issueCommentAutoApprovementHandler.handle(event);
        }

        return HttpStatus.OK;
    }
}
