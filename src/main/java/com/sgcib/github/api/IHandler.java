package com.sgcib.github.api;

public interface IHandler<TI, TO> {

    TO handle(TI param);
}
