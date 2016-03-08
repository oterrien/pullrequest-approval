package com.sgcib.github.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class PullRequestReviewEventHandler implements IEventHandler {

    @Autowired
    private Service service;

    @Override
    public void handle(String param) {
        service.display("PullRequestReviewEventHandler");
    }
}
