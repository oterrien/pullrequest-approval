package com.sgcib.github.api.component;

import com.sgcib.github.api.FilesUtils;
import lombok.Getter;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Properties;

@Component
public class StatusConfiguration {

    @Value("${status.context.pullrequest_approval.name}")
    @Getter
    private String contextPullRequestApprovalStatus;

    @Value("${status.context.do_not_merge_label.name}")
    @Getter
    private String contextDoNotMergeLabelStatus;

    @Value("${status.message.template.path}")
    private String messageTemplatePath;

    @Value("${status.message.pullrequest_approval.success.key}")
    private String messagePullRequestApprovalSuccessKey;

    @Value("${status.message.pullrequest_approval.pending.key}")
    private String messagePullRequestApprovalPendingKey;

    @Value("${status.message.pullrequest_approval.error.key}")
    private String messagePullRequestApprovalErrorKey;

    @Value("${status.message.do_not_merge.success.key}")
    private String messageDoNotMergeSuccessKey;

    @Value("${status.message.do_not_merge.error.key}")
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
