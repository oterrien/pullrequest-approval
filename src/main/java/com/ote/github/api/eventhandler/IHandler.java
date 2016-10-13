package com.ote.github.api.eventhandler;

public interface IHandler<TI, TO> {

    TO handle(TI param);
}
