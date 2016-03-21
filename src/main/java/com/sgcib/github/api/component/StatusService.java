package com.sgcib.github.api.component;

import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.json.Statuses;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private StatusConfiguration statusConfiguration;

    private ICommunicationService communicationService;

    @Autowired
    public StatusService(ICommunicationService communicationService) {
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

    public Status createStatus(Status.State state, String user, String context, RepositoryConfiguration remoteConfiguration) {

        Status status = new Status();
        status.setContext(context);
        status.setTargetUrl(StringUtils.EMPTY);
        status.setState(state.getValue());
        status.setDescription(computeDescription(state, user, context, remoteConfiguration));

        return status;
    }

    private String computeDescription(Status.State state, String user, String context, RepositoryConfiguration remoteConfiguration) {

        ContextType contextType = getContextType(context);

        switch (contextType) {
            case PULL_REQUEST_APPROVAL: {
                Map<String, String> map = new HashMap<>(1);
                map.put("user", user);
                switch (state) {
                    case SUCCESS:
                        return StrSubstitutor.replace(statusConfiguration.getMessagePullRequestApprovalSuccess(), map);
                    case PENDING:
                        return statusConfiguration.getMessagePullRequestApprovalPending();
                    case ERROR:
                    case FAILURE:
                        return StrSubstitutor.replace(statusConfiguration.getMessagePullRequestApprovalError(), map);
                }
                break;
            }
            case DO_NOT_MERGE: {
                Map<String, String> map = new HashMap<>(1);
                map.put("label", remoteConfiguration.getDoNotMergeLabelName());
                switch (state) {
                    case SUCCESS:
                        return StrSubstitutor.replace(statusConfiguration.getMessageDoNotMergeSuccess(), map);
                    case PENDING:
                    case ERROR:
                    case FAILURE:
                        return StrSubstitutor.replace(statusConfiguration.getMessageDoNotMergeError(), map);
                }
                break;
            }
        }
        return state.getValue();
    }

    public ContextType getContextType(String value) {

        if (Objects.equals(value, statusConfiguration.getContextPullRequestApprovalStatus())) {
            return ContextType.PULL_REQUEST_APPROVAL;
        }

        if (Objects.equals(value, statusConfiguration.getContextDoNotMergeLabelStatus())) {
            return ContextType.DO_NOT_MERGE;
        }

        return ContextType.NONE;
    }

    public enum ContextType {
        PULL_REQUEST_APPROVAL, DO_NOT_MERGE, NONE;
    }
}
