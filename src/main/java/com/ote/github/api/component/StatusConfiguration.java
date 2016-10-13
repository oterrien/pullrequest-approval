package com.ote.github.api.component;

import com.ote.github.api.FilesUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Properties;

@Component
public class StatusConfiguration {

    @Value("${status.context.pullrequest_approval}")
    @Getter
    private String contextPullRequestApprovalStatus;

    @Value("${status.context.do_not_merge_label}")
    @Getter
    private String contextDoNotMergeLabelStatus;

    @Value("${status.message.template-path}")
    private String messageTemplatePath;

    @Value("${status.message.key.pullrequest_approval.success}")
    private String messagePullRequestApprovalSuccessKey;

    @Value("${status.message.key.pullrequest_approval.pending}")
    private String messagePullRequestApprovalPendingKey;

    @Value("${status.message.key.pullrequest_approval.error}")
    private String messagePullRequestApprovalErrorKey;

    @Value("${status.message.key.do_not_merge.success}")
    private String messageDoNotMergeSuccessKey;

    @Value("${status.message.key.do_not_merge.error}")
    private String messageDoNotMergeErrorKey;

    @Getter
    private String messagePullRequestApprovalSuccess;

    @Getter
    private String messagePullRequestApprovalPending;

    @Getter
    private String messagePullRequestApprovalError;

    @Getter
    private String messageDoNotMergeSuccess;

    @Getter
    private String messageDoNotMergeError;

    @PostConstruct
    private void setUp() {

        try {
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(FilesUtils.readFileInClasspath(this.messageTemplatePath).getBytes()));

            this.messagePullRequestApprovalSuccess = properties.getProperty(this.messagePullRequestApprovalSuccessKey);
            this.messagePullRequestApprovalPending = properties.getProperty(this.messagePullRequestApprovalPendingKey);
            this.messagePullRequestApprovalError = properties.getProperty(this.messagePullRequestApprovalErrorKey);
            this.messageDoNotMergeSuccess = properties.getProperty(this.messageDoNotMergeSuccessKey);
            this.messageDoNotMergeError = properties.getProperty(this.messageDoNotMergeErrorKey);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
