package com.sgcib.github.api.eventhandler.configuration;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public final class IssueCommentConfiguration {

    @Value("${issue.comments.approval.list}")
    private String approvalComments;

    @Value("${issue.comments.rejection.list}")
    private String rejectionComments;

    @Value("${issue.comments.pending.list}")
    private String pendingComments;

    @Value("${remote.configuration.checked}")
    @Getter
    @Setter
    private boolean isRemoteConfigurationChecked;

    @Value("${remote.configuration.path}")
    @Getter
    @Setter
    private String remoteConfigurationPath;

    private List<String> approvalCommentsList = new ArrayList<>(10);

    private List<String> rejectionCommentsList = new ArrayList<>(10);

    private List<String> pendingCommentsList = new ArrayList<>(10);

    @PostConstruct
    public void setUp() {

        this.approvalCommentsList = Arrays.asList(approvalComments.split((",")));
        this.rejectionCommentsList = Arrays.asList(rejectionComments.split((",")));
        this.pendingCommentsList = Arrays.asList(pendingComments.split((",")));

        approvalComments = null;
        rejectionComments = null;
        pendingComments = null;

        Logger logger = LoggerFactory.getLogger(IssueCommentConfiguration.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Approval comments : " + this.approvalCommentsList);
            logger.debug("Rejection comments : " + this.rejectionCommentsList);
            logger.debug("Pending comments : " + this.pendingCommentsList);
        }
    }

    public Type getType(String comment) {

        if (pendingCommentsList.contains(comment))
            return Type.PENDING;

        if (rejectionCommentsList.contains(comment))
            return Type.REJECTION;

        if (approvalCommentsList.contains(comment))
            return Type.APPROVEMENT;

        return Type.NONE;
    }

    public enum Type {
        APPROVEMENT, REJECTION, PENDING, NONE;
    }
}
