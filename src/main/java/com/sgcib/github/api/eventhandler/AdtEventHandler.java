package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.configuration.RemoteConfiguration;
import com.sgcib.github.api.json.File;
import com.sgcib.github.api.json.Repository;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.json.Statuses;
import com.sgcib.github.api.service.ICommunicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Optional;

public abstract class AdtEventHandler<T> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(AdtEventHandler.class);

    protected Configuration configuration;

    protected ICommunicationService communicationService;

    protected Class<T> type;

    protected AdtEventHandler(Class<T> type, Configuration configuration, ICommunicationService communicationService) {
        this.type = type;
        this.configuration = configuration;
        this.communicationService = communicationService;
    }

    @Override
    public final HttpStatus handle(String event) {

        try {
            T obj = JsonUtils.parse(event, type);
            return this.handle(obj);
        } catch (IOException e) {
            return processError(new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Unable to parse the event", event));
        } catch (EventHandlerException e) {
            return processError(e);
        } finally {
            logger.info("finished");
        }
    }

    protected abstract HttpStatus handle(T obj) throws EventHandlerException;

    private HttpStatus processError(EventHandlerException e) {

        if (logger.isErrorEnabled()) {
            logger.error(e.getReason(), e.getInnerException());
        }

        if (logger.isDebugEnabled() && e.getEvent() != null) {
            logger.error("event in error : " + e.getEvent());
        }
        return e.getHttpStatus();
    }

    protected Status.State getCurrentState(String statusesUrl) {

        if (logger.isDebugEnabled()) {
            logger.debug("Finding whether current status is rejected or not");
        }

        Optional<Status> status = findLastStatus(statusesUrl);

        return status.isPresent() ?
                Status.State.of(status.get().getState()) :
                Status.State.PENDING;
    }

    private Optional<Status> findLastStatus(String statusesUrl) {
        try {

            String str = communicationService.get(statusesUrl);
            str = "{\"statuses\":" + str + "}";
            Statuses statuses = JsonUtils.parse(str, Statuses.class);
            return statuses.getStatuses().stream().filter(p -> p.getContext().equals(configuration.getStatusContext())).findFirst();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to retrieve last status", e);
            }
            return Optional.empty();
        }
    }

    protected Optional<RemoteConfiguration> getRemoteConfiguration(Repository repository) {

        String defaultBranch = repository.getDefaultBranch();
        String contentsUrl = repository.getContentsUrl();
        contentsUrl = contentsUrl.replace("{+path}", configuration.getRemoteConfigurationPath() + "?ref=" + defaultBranch);
        try {
            File file = JsonUtils.parse(communicationService.get(contentsUrl), File.class);
            String content = communicationService.get(file.getDownloadUrl());
            return Optional.of(new RemoteConfiguration(configuration, content));
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Unable to initialize remote configuration", e);
            }
            return Optional.empty();
        }
    }
}
