package org.jpromise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class FuturePromise<V> extends AbstractPromise<V> implements Runnable {
    private final Future<V> future;

    FuturePromise(Future<V> future) {
        this.future = future;
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
            complete(future.get());
        }
        catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause == null) {
                cause = exception;
            }
            completeWithException(cause);
        }
        catch (InterruptedException exception) {
            completeWithException(exception);
        }
    }
}
