package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.component.AuthorizationConfiguration;
import com.sgcib.github.api.component.IssueCommentConfiguration;
import com.sgcib.github.api.json.IssueCommentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.Serializable;

public abstract class AdtEventHandlerDispatcher<T extends Serializable> implements IHandler<String, HttpStatus> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected AuthorizationConfiguration authorizationConfiguration;

    protected Class<T> type;

    protected AdtEventHandlerDispatcher(Class<T> type) {
        this.type = type;
    }

    @Override
    public final HttpStatus handle(String eventString) {

        try {
            T event = JsonUtils.parse(eventString, type);

            if (isTechnicalUserAction(event)){
                return HttpStatus.OK;
            }

            return this.handle(event);
        } catch (IOException e) {
            return processError(new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Unable to parse the event"));
        } catch (EventHandlerException e) {
            return processError(e);
        }
    }

    protected abstract HttpStatus handle(T event);

    protected abstract boolean isTechnicalUserAction(T event);

    private HttpStatus processError(EventHandlerException e) {

        if (logger.isErrorEnabled()) {
            logger.error(e.getReason(), e.getCause());
        }
        return e.getHttpStatus();
    }
}
