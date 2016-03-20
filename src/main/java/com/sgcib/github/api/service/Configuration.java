package com.sgcib.github.api.service;

import com.sgcib.github.api.FilesUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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

    @Value("${context.pullrequest_approval.name}")
    @Getter
    private String pullRequestApprovalStatusContext;

    @Value("${context.do_not_merge_label.name}")
    @Getter
    private String doNotMergeLabelStatusContext;

    @Value("${message.status.template.path}")
    private String messageStatusTemplatePath;

    @Value("${message.status.pullrequest_approval.success.key}")
    private String messageStatusPullRequestApprovalSuccessKey;

    @Value("${message.status.pullrequest_approval.pending.key}")
    private String messageStatusPullRequestApprovalPendingKey;

    @Value("$message.status.pullrequest_approval.error.key}")
    private String messageStatusPullRequestApprovalErrorKey;

    @Value("${message.status.do_not_merge.success.key}")
    private String messageStatusDoNotMergeSuccessKey;

    @Value("${message.status.do_not_merge.error.key}")
    private String messageStatusDoNotMergeErrorKey;

    @Getter
    private String messageStatusPullRequestApprovalSuccess;

    @Getter
    private String messageStatusPullRequestApprovalPending;

    @Getter
    private String messageStatusPullRequestApprovalError;

    @Getter
    private String messageStatusDoNotMergeSuccess;

    @Getter
    private String messageStatusDoNotMergeError;

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

    @Value("${remote.configuration.key.do_not_merge.label}")
    @Getter
    private String remoteConfigurationDoNotMergeLabelKey;

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

        try {
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(FilesUtils.readFileInClasspath(this.messageStatusTemplatePath).getBytes()));
            this.messageStatusPullRequestApprovalSuccess = properties.getProperty(this.messageStatusPullRequestApprovalSuccessKey);
            this.messageStatusPullRequestApprovalPending = properties.getProperty(this.messageStatusPullRequestApprovalPendingKey);
            this.messageStatusPullRequestApprovalError = properties.getProperty(this.messageStatusPullRequestApprovalErrorKey);
            this.messageStatusDoNotMergeSuccess = properties.getProperty(this.messageStatusDoNotMergeSuccessKey);
            this.messageStatusDoNotMergeError = properties.getProperty(this.messageStatusDoNotMergeErrorKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
