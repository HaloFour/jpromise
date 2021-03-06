package org.jpromise;

import java.util.concurrent.Executor;

abstract class RejectionPromise<E extends Throwable, V> extends ContinuationPromise<V, V> {
    private final Class<E> exceptionClass;

    RejectionPromise(Promise<V> promise, Executor executor, Class<E> exceptionClass) {
        super(promise, executor);
        this.exceptionClass = exceptionClass;
    }

    @Override
    protected void completeComposed(V result) throws Throwable {
        complete(result);
    }

    @Override
    protected void completeComposedWithException(Throwable exception) throws Throwable {
        if (exceptionClass.isInstance(exception)) {
            E typed = exceptionClass.cast(exception);
            handle(typed);
        }
        else {
            super.completeComposedWithException(exception);
        }
    }

    protected abstract void handle(E exception) throws Throwable;
}
