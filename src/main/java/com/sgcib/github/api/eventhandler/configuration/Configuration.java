package com.sgcib.github.api.eventhandler.configuration;

import com.sgcib.github.api.FilesUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Component
public final class Configuration {

    @Autowired
    private ApplicationContext servletContext;

    @Value("${handler.authorization.login}")
    private String login;

    @Value("${handler.authorization.password}")
    private String password;

    @Value("${issue.comments.list.approval}")
    private String approvalComments;

    @Value("${issue.comments.list.auto_approval}")
    private String autoApprovalComments;

    @Value("${issue.comments.list.rejection}")
    private String rejectionComments;

    @Value("${issue.comments.list.pending}")
    private String pendingComments;

    @Value("${status.context}")
    @Getter
    private String statusContext;

    @Value("${remote.configuration.checked}")
    @Getter
    private boolean isRemoteConfigurationChecked;

    @Value("${remote.configuration.path}")
    @Getter
    private String remoteConfigurationPath;

    @Value("${remote.configuration.key.auto_approval.authorized}")
    @Getter
    private String remoteConfigurationAutoApprovalKey;

    @Value("${default.auto_approval.authorized}")
    @Getter
    private boolean isAutoApprovalAuthorizedByDefault;

    @Value("${file.auto_approval.alert.message.template}")
    @Getter
    private String autoApprovalAlertMessageTemplateFileName;

    @Value("${file.auto_approval.report.message.template}")
    @Getter
    private String autoApprovaReportMessageTemplateFileName;

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

    @Getter
    private ResponseEntity<String> indexPage;

    @PostConstruct
    private void setUp() {

        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        httpHeaders.set("Authorization", authHeader);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        approvalCommentsList = Arrays.asList(approvalComments.toLowerCase().split((",")));
        rejectionCommentsList = Arrays.asList(rejectionComments.toLowerCase().split((",")));
        pendingCommentsList = Arrays.asList(pendingComments.toLowerCase().split((",")));
        autoApprovalCommentsList = Arrays.asList(autoApprovalComments.toLowerCase().split((",")));

        final Map<String, String> param = new HashMap<>(10);
        param.put("status.context", getStatusContext());
        param.put("issue.comments.list.approval", getApprovalCommentsList().stream().collect(Collectors.joining(" or ")));
        param.put("issue.comments.list.rejection", getRejectionCommentsList().stream().collect(Collectors.joining(" or ")));
        param.put("issue.comments.list.pending", getPendingCommentsList().stream().collect(Collectors.joining(" or ")));
        param.put("issue.comments.list.auto_approval", getAutoApprovalCommentsList().stream().collect(Collectors.joining(" or ")));
        param.put("remote.configuration.auto_approval.authorized.key", getRemoteConfigurationAutoApprovalKey());
        param.put("remote.configuration.path", getRemoteConfigurationPath());

        try {
            indexPage = new ResponseEntity<>(FilesUtils.readFileInClasspath("index.html", param), HttpStatus.OK);
        } catch (Exception e) {
            indexPage = new ResponseEntity<>("An error occured while providing information", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    public Type getType(String comment) {

       String commentLowerCase = comment.toLowerCase();

        if (pendingCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))){
            return Type.PENDING;
        }

        if (rejectionCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))){
            return Type.REJECTION;
        }

        if (autoApprovalCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))){
            return Type.AUTO_APPROVEMENT;
        }

        if (approvalCommentsList.stream().anyMatch(word -> commentLowerCase.contains(word))){
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
