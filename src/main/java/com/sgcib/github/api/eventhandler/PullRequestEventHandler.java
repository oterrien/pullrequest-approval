package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.payloayd.PullRequestPayload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class PullRequestEventHandler extends AdtEventHandler<PullRequestPayload> implements IEventHandler {

    public PullRequestEventHandler() {
        super(PullRequestPayload.class);
    }

    @Override
    public void handle(PullRequestPayload event) throws IOException {

    }
}
