package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.JSOnParser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Olivier on 11/03/2016.
 */
public abstract class AdtEventHandler<T> implements IEventHandler{

    protected static Logger logger = Logger.getLogger(IEventHandler.class.getName());

    @Autowired
    private JSOnParser jsonParser;

    private Class<T> type;

    protected AdtEventHandler(Class<T> type){
        this.type = type;
    }

    @Override
    public final void handle(String event) {

        logger.info(event);

        try {
            T obj = jsonParser.parse(this.type, event);
            handle(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void handle(T obj);
}
