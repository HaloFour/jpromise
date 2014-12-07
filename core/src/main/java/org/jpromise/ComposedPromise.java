package org.jpromise;

import org.jpromise.functions.OnCompleted;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

abstract class ComposedPromise<V_IN, V_OUT> extends AbstractPromise<V_OUT> implements OnCompleted<V_IN> {
    private final Executor executor;

    protected ComposedPromise(Executor executor) {
        this.executor = executor;
    }

    @Override
    public final void completed(final Promise<V_IN> promise, final V_IN result, final Throwable exception) throws Throwable {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (promise.state()) {
                        case RESOLVED:
                            completed(result);
                            break;
                        case REJECTED:
                            completed(exception);
                            break;
                    }
                }
                catch (Throwable thrown) {
                    setException(thrown);
                }
            }
        });
    }

    protected abstract void completed(V_IN result) throws Throwable;

    protected void completed(Throwable exception) throws Throwable {
        setException(exception);
    }

    protected void setFuture(Future<V_OUT> future) {
        if (future == null) {
            setPromise(null);
        }
        else {
            Promise<V_OUT> promise;
            if (future instanceof Promise) {
                promise = (Promise<V_OUT>) future;
            } else {
                promise = new FuturePromise<>(future);
            }
            setPromise(promise);
        }
    }

    protected void setPromise(Promise<V_OUT> promise) {
        if (promise == null) {
            set(null);
        }
        else {
            promise.whenCompleted(new OnCompleted<V_OUT>() {
                @Override
                public void completed(Promise<V_OUT> promise, V_OUT result, Throwable exception) throws Throwable {
                    switch (promise.state()) {
                        case RESOLVED:
                            set(result);
                            break;
                        case REJECTED:
                            setException(exception);
                            break;
                    }
                }
            });
        }
    }
}
