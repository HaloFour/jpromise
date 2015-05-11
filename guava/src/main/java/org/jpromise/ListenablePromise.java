package org.jpromise;

import com.google.common.util.concurrent.ListenableFuture;
import org.jpromise.functions.OnCompleted;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class ListenablePromise<R> extends AbstractPromise<R> implements ListenableFuture<R> {
    protected ListenablePromise() { }

    public ListenablePromise(final ListenableFuture<R> future) {
        if (future == null) throw new IllegalArgumentException(mustNotBeNull("future"));
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    complete(future.get());
                }
                catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    if (cause != null) {
                        completeWithException(cause);
                    }
                    else {
                        completeWithException(exception);
                    }
                }
                catch (Throwable exception) {
                    completeWithException(exception);
                }
            }
        }, PromiseExecutors.CURRENT_THREAD);
    }

    @Override
    public void addListener(final Runnable listener, Executor executor) {
        if (listener == null) throw new IllegalArgumentException(mustNotBeNull("listener"));
        if (executor == null) {
            executor = PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR;
        }
        this.whenCompleted(executor, new OnCompleted<R>() {
            @Override
            public void completed(Promise<R> promise, R result, Throwable exception) throws Throwable {
                listener.run();
            }
        });
    }
}
