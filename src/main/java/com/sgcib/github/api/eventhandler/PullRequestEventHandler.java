package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.payloayd.PullRequest;
import org.springframework.stereotype.Component;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class PullRequestEventHandler extends AdtEventHandler<PullRequest> implements IEventHandler {

    public PullRequestEventHandler() {
        super(PullRequest.class);
    }

    @Override
    public void handle(PullRequest event) {


        System.out.println(event.getAction());
    }
}
