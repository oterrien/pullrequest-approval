package com.sgcib.github.api.eventhandler.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public final class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    @Value("${handler.authorization.login}")
    private String login;

    @Value("${handler.authorization.password}")
    private String password;

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

    @Getter
    private HttpHeaders httpHeaders = new HttpHeaders();

    private List<String> approvalCommentsList = new ArrayList<>(10);

    private List<String> rejectionCommentsList = new ArrayList<>(10);

    private List<String> pendingCommentsList = new ArrayList<>(10);

    @PostConstruct
    private void setUp() {

        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        httpHeaders.set("Authorization", authHeader);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        this.approvalCommentsList = Arrays.asList(approvalComments.split((",")));
        this.rejectionCommentsList = Arrays.asList(rejectionComments.split((",")));
        this.pendingCommentsList = Arrays.asList(pendingComments.split((",")));

        login = null;
        password = null;
        approvalComments = null;
        rejectionComments = null;
        pendingComments = null;

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
