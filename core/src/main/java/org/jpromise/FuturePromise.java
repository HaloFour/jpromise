package org.jpromise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class FuturePromise<V> extends AbstractPromise<V> implements Runnable {
    private final Future<V> future;
    private final long timeout;
    private final TimeUnit timeUnit;

    FuturePromise(Future<V> future) {
        this(future, Long.MIN_VALUE, null);
    }

    FuturePromise(Future<V> future, long timeout, TimeUnit timeUnit) {
        this.future = future;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        if (future.isDone()) {
            this.run();
        }
        else {
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public void run() {
        try {
            if (timeout == Long.MIN_VALUE) {
                complete(future.get());
            }
            else {
                complete(future.get(timeout, timeUnit));
            }
        }
        catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause == null) {
                cause = exception;
            }
            completeWithException(cause);
        }
        catch (TimeoutException | InterruptedException exception) {
            completeWithException(exception);
        }
    }
}
