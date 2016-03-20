package com.sgcib.github.api.configuration;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
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

    @Value("${issue.comments.list.approval}")
    private String approvalComments;

    @Value("${issue.comments.list.auto_approval}")
    private String autoApprovalComments;

    @Value("${issue.comments.list.rejection}")
    private String rejectionComments;

    @Value("${issue.comments.list.pending}")
    private String pendingComments;

    @Value("${status.context.pullrequest_approval}")
    @Getter
    private String pullRequestApprovalStatusContext;

    @Value("${status.context.donotmerge_label}")
    @Getter
    private String doNotMergeLabelStatusContext;

    @Value("${handler.authorization.login}")
    @Getter
    private String technicalUserLogin;

    @Value("${handler.authorization.password}")
    private String technicalUserPassword;

    @Value("${file.auto_approval.advice.message.template}")
    @Getter
    private String autoApprovalAdviceMessageTemplateFileName;

    @Value("${file.auto_approval.alert.message.template}")
    @Getter
    private String autoApprovalAlertMessageTemplateFileName;

    @Value("${remote.configuration.path}")
    @Getter
    private String remoteConfigurationPath;

    @Value("${remote.configuration.key.auto_approval.authorized}")
    @Getter
    private String remoteConfigurationAutoApprovalKey;

    @Value("${remote.configuration.key.payload.url}")
    @Getter
    private String remoteConfigurationPayloadUrlKey;

    @Getter
    private HttpHeaders httpHeaders = new HttpHeaders();

    @Getter
    private List<String> approvalCommentsList = new ArrayList<>(10);

    @Getter
    private List<String> rejectionCommentsList = new ArrayList<>(10);

    @Getter
    private List<String> pendingCommentsList = new ArrayList<>(10);

    @Getter
    private List<String> autoApprovalCommentsList = new ArrayList<>(10);

    @PostConstruct
    private void setUp() {

        String auth = technicalUserLogin + ":" + technicalUserPassword;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        this.httpHeaders.set("Authorization", authHeader);
        this.httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        this.approvalCommentsList = Arrays.asList(approvalComments.toLowerCase().split((",")));
        this.rejectionCommentsList = Arrays.asList(rejectionComments.toLowerCase().split((",")));
        this.pendingCommentsList = Arrays.asList(pendingComments.toLowerCase().split((",")));
        this.autoApprovalCommentsList = Arrays.asList(autoApprovalComments.toLowerCase().split((",")));
    }

    public IssueCommentType getType(String comment) {

        String commentLowerCase = comment.toLowerCase();

        if (pendingCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))) {
            return IssueCommentType.PENDING;
        }

        if (rejectionCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))) {
            return IssueCommentType.REJECTION;
        }

        if (autoApprovalCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))) {
            return IssueCommentType.AUTO_APPROVEMENT;
        }

        if (approvalCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))) {
            return IssueCommentType.APPROVEMENT;
        }

        return IssueCommentType.NONE;
    }

    public enum IssueCommentType {

        APPROVEMENT("approved"),
        REJECTION("rejected"),
        PENDING("pending"),
        AUTO_APPROVEMENT("approved"),
        NONE(StringUtils.EMPTY);

        @Getter
        private String value;

        IssueCommentType(String value) {
            this.value = value;
        }
    }
}
