package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.eventhandler.configuration.Configuration;
import com.sgcib.github.api.eventhandler.configuration.RemoteConfiguration;
import com.sgcib.github.api.payloayd.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public abstract class AdtEventHandler<T> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(AdtEventHandler.class);

    private final Class<T> type;

    @Autowired
    protected Configuration configuration;

    @Autowired
    protected CommunicationService communicationService;

    @Autowired
    protected JsonService jsonService;

    protected AdtEventHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public final HttpStatus handle(String event) {

        if (logger.isDebugEnabled()) {
            logger.debug(type.getSimpleName() + " event to be processed : " + event);
        }

        try {
            T obj = jsonService.parse(event, this.type);
            return this.handle(obj);
        } catch (IOException e) {
            return processError(new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Unable to parse the event", event));
        } catch (EventHandlerException e) {
            return processError(e);
        }
    }

    protected abstract HttpStatus handle(T obj) throws EventHandlerException;

    private HttpStatus processError(EventHandlerException e) {

        if (logger.isErrorEnabled()) {
            logger.error(e.getReason(), e.getInnerException());
        }

        if (logger.isDebugEnabled() && e.getEvent() != null) {
            logger.error(type.getSimpleName() + " event in error : " + e.getEvent());
        }
        return e.getHttpStatus();
    }

    protected Status.State getCurrentState(String statusesUrl, String remoteRepositoryName) {

        if (logger.isDebugEnabled()) {
            logger.debug(remoteRepositoryName + " : finding whether current status is rejected or not");
        }

        Optional<Status> status = findLastStatus(statusesUrl, remoteRepositoryName);

        if (status.isPresent()) {
            return Status.State.of(status.get().getState());
        }

        return Status.State.PENDING;
    }

    private Optional<Status> findLastStatus(String statusesUrl, String remoteRepositoryName) {
        try {

            String str = communicationService.get(statusesUrl, remoteRepositoryName);
            str = "{\"statuses\":" + str + "}";
            Statuses statuses = new JsonService().parse(str, Statuses.class);
            return statuses.getStatuses().stream().filter(p -> p.getContext().equals(Status.State.CONTEXT)).findFirst();
        } catch (Exception e) {
            logger.warn(remoteRepositoryName + " : unable to retrieve remote configuration file", e);
            return Optional.empty();
        }
    }

    protected Optional<RemoteConfiguration> getRemoteConfiguration(Repository repository) {

        String defaultBranch = repository.getDefaultBranch();
        String contentsUrl = repository.getContentsUrl();
        contentsUrl = contentsUrl.replace("{+path}", configuration.getRemoteConfigurationPath() + "?ref=" + defaultBranch);

        try {
            File file = jsonService.parse(communicationService.get(contentsUrl, repository.getName()), File.class);
            String content = communicationService.get(file.getDownloadUrl(), repository.getName());

            try {
                Properties prop = new Properties();
                prop.load(new ByteArrayInputStream(content.getBytes()));
                return Optional.of(new RemoteConfiguration(prop));
            } catch (IOException e) {
                logger.error(repository.getName() + " : error while parsing remote configuration content " + content, e);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error(repository.getName() + " : unable to retrieve remote configuration file", e);
            return Optional.empty();
        }
    }
}
