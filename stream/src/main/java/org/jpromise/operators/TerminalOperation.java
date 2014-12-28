package org.jpromise.operators;

public interface TerminalOperation<V, R> {
    void start() throws Throwable;
    void fulfilled(V result) throws Throwable;
    R completed() throws Throwable;
}
