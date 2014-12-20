package org.jpromise;

import java.util.concurrent.*;

class FuturePromise<V> extends AbstractPromise<V> {
    private final Future<V> future;
    private final Long timeout;
    private final TimeUnit timeUnit;

    FuturePromise(Executor executor, Future<V> future) {
        this.future = future;
        this.timeout = null;
        this.timeUnit = null;
        start(executor);
    }

    FuturePromise(Executor executor, Future<V> future, long timeout, TimeUnit timeUnit) {
        this.future = future;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        start(executor);
    }

    private void start(Executor executor) {
        FuturePromiseRunnable runnable = new FuturePromiseRunnable();
        if (future.isDone()) {
            runnable.run();
        }
        else {
            executor.execute(runnable);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning)
                && completeWithException(new CancellationException());
    }

    private class FuturePromiseRunnable implements Runnable {
        @Override
        public void run() {
            try {
                if (timeout == null) {
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
            catch (Throwable exception) {
                completeWithException(exception);
            }
        }
    }
}
