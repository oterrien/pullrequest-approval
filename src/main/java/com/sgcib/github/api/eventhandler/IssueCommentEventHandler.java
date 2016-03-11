package com.sgcib.github.api.eventhandler;

import org.eclipse.egit.github.core.event.IssueCommentPayload;
import org.springframework.stereotype.Component;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class IssueCommentEventHandler extends AdtEventHandler<IssueCommentPayload> implements IEventHandler {

    public IssueCommentEventHandler() {
        super(IssueCommentPayload.class);
    }

    @Override
    public void handle(IssueCommentPayload event) {

        System.out.println(event.getAction());
    }
}
