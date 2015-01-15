package org.jpromise;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

class RunnablePromise<V> extends AbstractPromise<V> implements Runnable {
    private final Callable<V> task;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private Thread thread;

    public RunnablePromise(Callable<V> task) {
        if (task == null) throw new IllegalArgumentException(mustNotBeNull("task"));
        this.task = task;
    }

    public RunnablePromise(final Runnable task, final V result) {
        if (task == null) throw new IllegalArgumentException(mustNotBeNull("task"));
        this.task = new Callable<V>() {
            @Override
            public V call() throws Exception {
                task.run();
                return result;
            }
        };
    }

    @Override
    public void run() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        try {
            thread = Thread.currentThread();
            V result = task.call();
            this.complete(result);
        }
        catch (Throwable exception) {
            if (exception instanceof RuntimeException) {
                Throwable cause = exception.getCause();
                if (cause != null) {
                    exception = cause;
                }
            }
            this.completeWithException(exception);
        }
        finally {
            thread = null;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (started.compareAndSet(false, true)) {
            return super.cancel(mayInterruptIfRunning);
        }
        if (super.cancel(mayInterruptIfRunning)) {
            if (mayInterruptIfRunning) {
                Thread thread = this.thread;
                if (thread != null && thread != Thread.currentThread()) {
                    thread.interrupt();
                }
            }
            return true;
        }
        return false;
    }
}
