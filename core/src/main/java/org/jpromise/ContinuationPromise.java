package org.jpromise;

import org.jpromise.functions.OnCompleted;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

abstract class ContinuationPromise<V_IN, V_OUT> extends AbstractPromise<V_OUT> implements Continuation<V_IN> {
    private final Executor executor;
    private final PromiseCallbackListener callback;
    private Promise<V_OUT> composed;
    private Thread callbackThread;
    private boolean cancelled;

    protected ContinuationPromise(Promise<V_IN> promise, Executor executor) {
        this.executor = executor;
        this.callback = PromiseComposition.LISTENER.composingCallback(promise, this);
    }

    @Override
    public final void completed(final Promise<V_IN> promise, final V_IN result, final Throwable exception) {
        if (cancelled) {
            return;
        }
        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (cancelled) {
                        return;
                    }
                    try (AutoCloseable ignored = callback.invokingPromiseCallback(promise, ContinuationPromise.this, result, exception)) {
                        callbackThread = Thread.currentThread();
                        switch (promise.state()) {
                            case RESOLVED:
                                completeComposed(result);
                                break;
                            case REJECTED:
                                completeComposedWithException(exception);
                                break;
                        }
                    }
                    catch (Throwable thrown) {
                        completeWithException(thrown);
                    }
                    finally {
                        callbackThread = null;
                    }
                }
            });
        }
        catch (Throwable thrown) {
            completeWithException(thrown);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (super.cancel(mayInterruptIfRunning)) {
            cancelled = true;
            Promise<V_OUT> composed = this.composed;
            Thread callbackThread = this.callbackThread;
            if (mayInterruptIfRunning && callbackThread != null) {
                callbackThread.interrupt();
            }
            return composed == null || composed.cancel(mayInterruptIfRunning);
        }
        return false;
    }

    protected abstract void completeComposed(V_IN result) throws Throwable;

    protected void completeComposedWithException(Throwable exception) throws Throwable {
        completeWithException(exception);
    }

    protected void completeWithFuture(Future<V_OUT> future) {
        if (future == null) {
            completeWithPromise(null);
        }
        else {
            Promise<V_OUT> promise;
            if (future instanceof Promise) {
                promise = (Promise<V_OUT>) future;
            }
            else {
                promise = new FuturePromise<>(PromiseExecutors.NEW_THREAD, future);
            }
            completeWithPromise(promise);
        }
    }

    protected void completeWithPromise(Promise<V_OUT> promise) {
        if (cancelled) {
            return;
        }
        if (promise == null) {
            complete(null);
        }
        else {
            composed = promise;
            promise.whenCompleted(this.executor, new OnCompleted<V_OUT>() {
                @Override
                public void completed(Promise<V_OUT> promise, V_OUT result, Throwable exception) throws Throwable {
                    if (cancelled) {
                        return;
                    }
                    switch (promise.state()) {
                        case RESOLVED:
                            complete(result);
                            break;
                        case REJECTED:
                            completeWithException(exception);
                            break;
                    }
                }
            });
        }
    }
}
