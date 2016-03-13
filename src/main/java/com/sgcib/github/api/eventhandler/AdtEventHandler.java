package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.eventhandler.configuration.HandlerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

public abstract class AdtEventHandler<T> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(AdtEventHandler.class);

    private final Class<T> type;

    //TODO : what to do when repository is private -> no read right
    protected RestTemplate restTemplate = new RestTemplate();

    @Autowired
    @Lazy(false)
    protected JSOnService jsonService;

    @Autowired
    @Lazy(false)
    protected HandlerConfiguration handlerConfiguration;

    protected AdtEventHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public final HttpStatus handle(String event) {

        if (logger.isDebugEnabled()) {
            logger.debug(type.getSimpleName() + " event to be processed : " + event);
        }

        try {
            T obj = jsonService.parse(this.type, event);
            return this.handle(obj);
        } catch (IOException e) {
            return processError(new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Unable to parse the event", event));
        } catch (EventHandlerException e) {
            if (e.getEvent() == null) {
                e.setEvent(event);
            }
            return processError(e);
        }
    }

    private HttpStatus processError(EventHandlerException e) {

        if (logger.isErrorEnabled()) {
            logger.error(e.getReason(), e.getInnerException());
        }

        if (logger.isDebugEnabled() && e.getEvent() != null) {
            logger.debug(type.getSimpleName() + " event in error : " + e.getEvent());
        }
        return e.getHttpStatus();
    }

    protected abstract HttpStatus handle(T obj) throws EventHandlerException;

    protected HttpStatus postStatus(String url, Status status, String remoteRepositoryName) throws EventHandlerException {

        String statusStr = null;
        try {
            statusStr = jsonService.serialize(status);

            if (logger.isInfoEnabled()) {
                logger.info(remoteRepositoryName + " : posting status : " + statusStr + " to " + url);
            }

            restTemplate.postForObject(url, new HttpEntity<>(statusStr, this.handlerConfiguration.getHttpHeaders()), String.class);

            return HttpStatus.OK;

        } catch (RestClientException e) {
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST, remoteRepositoryName + " : error while posting status : " + url, statusStr);
        } catch (IOException e) {
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, remoteRepositoryName + " : error while serializing status : " + url, status.toString());
        }
    }

    protected Status.State getCurrentState(String statusesUrl, String remoteRepositoryName) {

        if (logger.isDebugEnabled()) {
            logger.debug(remoteRepositoryName + " : finding whether current status is rejected or not");
        }

        Optional<Status> status = Status.findLastStatus(statusesUrl, remoteRepositoryName);

        if (status.isPresent()) {
            return Status.State.of(status.get().getState());
        }

        return Status.State.PENDING;
    }
}
