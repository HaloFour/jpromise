package org.jpromise;

import java.util.concurrent.*;

class FuturePromise<V> extends AbstractPromise<V> implements Runnable {
    private final Future<V> future;
    private final long timeout;
    private final TimeUnit timeUnit;

    FuturePromise(Executor executor, Future<V> future) {
        this(executor, future, Long.MIN_VALUE, null);
    }

    FuturePromise(Executor executor, Future<V> future, long timeout, TimeUnit timeUnit) {
        this.future = future;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        if (future.isDone()) {
            this.run();
        }
        else {
            executor.execute(this);
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
