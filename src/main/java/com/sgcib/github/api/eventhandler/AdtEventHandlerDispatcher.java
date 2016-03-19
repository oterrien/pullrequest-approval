package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.IHandler;
import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.service.ICommunicationService;
import com.sgcib.github.api.service.RemoteConfigurationService;
import com.sgcib.github.api.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.Serializable;

public abstract class AdtEventHandlerDispatcher<T extends Serializable> implements IHandler<String, HttpStatus> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final Configuration configuration;

    protected Class<T> type;

    protected AdtEventHandlerDispatcher(Class<T> type, Configuration configuration) {
        this.type = type;
        this.configuration = configuration;
    }

    @Override
    public final HttpStatus handle(String event) {

        try {
            T obj = JsonUtils.parse(event, type);
            return this.handle(obj);
        } catch (IOException e) {
            return processError(new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Unable to parse the event"));
        } catch (EventHandlerException e) {
            return processError(e);
        }
    }

    protected abstract HttpStatus handle(T obj);

    private HttpStatus processError(EventHandlerException e) {

        if (logger.isErrorEnabled()) {
            logger.error(e.getReason(), e.getCause());
        }
        return e.getHttpStatus();
    }
}
