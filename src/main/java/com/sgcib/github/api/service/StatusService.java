package com.sgcib.github.api.service;

import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.json.Statuses;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public final class StatusService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusService.class);

    private Configuration configuration;

    private ICommunicationService communicationService;

    @Autowired
    public StatusService(Configuration configuration, ICommunicationService communicationService) {
        this.configuration = configuration;
        this.communicationService = communicationService;
    }

    public Status.State getCurrentState(String statusesUrl, String context) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finding whether current status is rejected or not");
        }

        Optional<Status> status = findLastStatus(statusesUrl, context);

        return status.isPresent() ?
                Status.State.of(status.get().getState()) :
                Status.State.PENDING;
    }

    public Optional<Status> findLastStatus(String statusesUrl, String context) {
        try {

            String str = communicationService.get(statusesUrl);
            str = "{\"statuses\":" + str + "}";
            Statuses statuses = JsonUtils.parse(str, Statuses.class);
            return statuses.getStatuses().stream().filter(p -> p.getContext().equals(context)).findFirst();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to retrieve last status from " + statusesUrl, e);
            }
            return Optional.empty();
        }
    }

    public Status createStatus(Status.State state, String user, String context, RemoteConfiguration remoteConfiguration) {

        Status status = new Status();
        status.setContext(context);
        status.setTargetUrl(remoteConfiguration.getPayloadUrl());
        status.setState(state.getValue());
        status.setDescription(computeDescription(state, user, context, remoteConfiguration));

        return status;
    }

    private String computeDescription(Status.State state, String user, String context, RemoteConfiguration remoteConfiguration) {

        switch (getContext(context)) {
            case PULL_REQUEST_APPROVAL: {
                Map<String, String> map = new HashMap<>(1);
                map.put("user", user);
                switch (state) {
                    case SUCCESS:
                        return StrSubstitutor.replace(configuration.getMessageStatusPullRequestApprovalSuccess(), map);
                    case PENDING:
                        return configuration.getMessageStatusPullRequestApprovalPending();
                    case ERROR:
                    case FAILURE:
                        return StrSubstitutor.replace(configuration.getMessageStatusPullRequestApprovalError(), map);
                }
                break;
            }
            case DO_NOT_MERGE: {
                Map<String, String> map = new HashMap<>(1);
                map.put("label", remoteConfiguration.getDoNotMergeLabelName());
                switch (state) {
                    case SUCCESS:
                        return StrSubstitutor.replace(configuration.getMessageStatusDoNotMergeSuccess(), map);
                    case PENDING:
                    case ERROR:
                    case FAILURE:
                        return StrSubstitutor.replace(configuration.getMessageStatusPullRequestApprovalError(), map);
                }
                break;
            }
        }
        return state.getValue();
    }

    public Context getContext(String value) {

        if (Objects.equals(value, configuration.getPullRequestApprovalStatusContext())) {
            return Context.PULL_REQUEST_APPROVAL;
        }

        if (Objects.equals(value, configuration.getDoNotMergeLabelStatusContext())) {
            return Context.DO_NOT_MERGE;
        }

        return Context.NONE;
    }

    enum Context {
        PULL_REQUEST_APPROVAL, DO_NOT_MERGE, NONE;
    }
}
