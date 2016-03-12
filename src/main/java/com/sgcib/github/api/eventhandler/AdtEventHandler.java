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

/**
 * Created by Olivier on 11/03/2016.
 */
public abstract class AdtEventHandler<T> implements IEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(AdtEventHandler.class);

    private final Class<T> type;

    @Autowired
    @Lazy(false)
    protected JSOnService jsonService;

    @Autowired
    @Lazy(false)
    protected HandlerConfiguration handlerConfiguration;

    private RestTemplate restTemplate = new RestTemplate();

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
            if (e.getEvent() == null)
                e.setEvent(event);
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

    protected Status generateStatus(Status.State state, Optional<String> user) {

        Status status = new Status();
        status.setContext("manual/pullrequest-approval");
        status.setTargetUrl("");
        status.setDescription(state.getDescription(user));
        status.setState(state.getState());

        return status;
    }

    protected HttpStatus postStatus(String url, Status status) throws EventHandlerException {

        String statusStr = null;
        try {
            statusStr = jsonService.serialize(status);

            if (logger.isInfoEnabled()) {
                logger.info("Posting status : " + statusStr + " to " + url);
            }

            restTemplate.postForObject(url, new HttpEntity<>(statusStr, this.handlerConfiguration.getHttpHeaders()), String.class);

            return HttpStatus.OK;

        } catch (RestClientException e) {
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST, "Error while posting status : " + url, statusStr);
        } catch (IOException e) {
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while serializing status : " + url, status.toString());
        }
    }
}
