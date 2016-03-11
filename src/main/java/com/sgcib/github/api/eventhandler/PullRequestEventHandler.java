package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.JSOnParser;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class PullRequestEventHandler extends AdtEventHandler<PullRequestPayload> implements IEventHandler {

    public PullRequestEventHandler() {
        super(PullRequestPayload.class);
    }

    @Override
    public void handle(PullRequestPayload event) {


        System.out.println(event.getAction());
    }
}
