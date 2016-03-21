package com.sgcib.github.api.component;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public final class IssueCommentConfiguration {

    @Value("${issue.comments.list.approval}")
    private String approvalComments;

    @Value("${issue.comments.list.auto_approval}")
    private String autoApprovalComments;

    @Value("${issue.comments.list.rejection}")
    private String rejectionComments;

    @Value("${issue.comments.list.pending}")
    private String pendingComments;

    @Value("${file.auto_approval.advice.message.template}")
    @Getter
    private String autoApprovalAdviceMessageTemplateFileName;

    @Value("${file.auto_approval.alert.message.template}")
    @Getter
    private String autoApprovalAlertMessageTemplateFileName;

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

        this.approvalCommentsList = Arrays.asList(approvalComments.toLowerCase().split(","));
        this.rejectionCommentsList = Arrays.asList(rejectionComments.toLowerCase().split(","));
        this.pendingCommentsList = Arrays.asList(pendingComments.toLowerCase().split(","));
        this.autoApprovalCommentsList = Arrays.asList(autoApprovalComments.toLowerCase().split((",")));
    }

    public Type getType(String comment) {

        String commentLowerCase = comment.toLowerCase();

        if (pendingCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))) {
            return Type.PENDING;
        }

        if (rejectionCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))) {
            return Type.REJECTION;
        }

        if (autoApprovalCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))) {
            return Type.AUTO_APPROVEMENT;
        }

        if (approvalCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))) {
            return Type.APPROVEMENT;
        }

        return Type.NONE;
    }

    public enum Type {

        APPROVEMENT("approved"),
        REJECTION("rejected"),
        PENDING("pending"),
        AUTO_APPROVEMENT("approved"),
        NONE(StringUtils.EMPTY);

        @Getter
        private String value;

        Type(String value) {
            this.value = value;
        }
    }
}
