package com.sgcib.github.api.eventhandler;

public interface IHandler<TI, TO> {

    TO handle(TI param);
}
