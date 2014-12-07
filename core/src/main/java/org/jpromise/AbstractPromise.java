package org.jpromise;

import org.jpromise.functions.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public abstract class AbstractPromise<V> extends Promise<V> {
    private final Object lock = new Object();
    private final CountDownLatch latch = new CountDownLatch(1);
    private final List<OnCompleted<V>> callbacks = new LinkedList<>();
    private PromiseState state = PromiseState.PENDING;
    private V result;
    private Throwable exception;

    @Override
    public PromiseState state() {
        return state;
    }

    @Override
    public boolean isCancelled() {
        return (exception instanceof CancellationException);
    }

    @Override
    public boolean isDone() {
        return (state != PromiseState.PENDING);
    }

    @Override
    public boolean isResolved() {
        return (state == PromiseState.RESOLVED);
    }

    @Override
    public boolean isRejected() {
        return (state == PromiseState.REJECTED);
    }

    @Override
    public String toString() {
        switch (state) {
            case RESOLVED:
                return String.format("[RESOLVED]: %s", result);
            case REJECTED:
                return String.format("[REJECTED]: %s", exception);
            default:
                return "[PENDING]";
        }
    }

    @Override
    public Promise<V> then(Executor executor, final OnResolved<? super V> action) {
        Arg.ensureNotNull(executor, "executor");
        if (action == null) {
            return this;
        }
        return registerCallback(new ComposedPromise<V, V>(executor) {
            @Override
            protected void completed(V result) throws Throwable {
                action.resolved(result);
                complete(result);
            }
        });
    }

    @Override
    public <V_APPLIED> Promise<V_APPLIED> thenApply(Executor executor, final OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        Arg.ensureNotNull(executor, "executor");
        Arg.ensureNotNull(function, "function");
        return registerCallback(new ComposedPromise<V, V_APPLIED>(executor) {
            @Override
            protected void completed(V result) throws Throwable {
                complete(function.resolved(result));
            }
        });
    }

    @Override
    public <V_COMPOSED> Promise<V_COMPOSED> thenCompose(Executor executor, final OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        Arg.ensureNotNull(executor, "executor");
        Arg.ensureNotNull(function, "function");
        return registerCallback(new ComposedPromise<V, V_COMPOSED>(executor) {
            @Override
            protected void completed(V result) throws Throwable {
                completeWithFuture(function.resolved(result));
            }
        });
    }

    @Override
    public <E extends Throwable> Promise<V> rejected(Class<E> exceptionClass, Executor executor, final OnRejected<? super E> action) {
        Arg.ensureNotNull(exceptionClass, "exceptionClass");
        Arg.ensureNotNull(executor, "executor");
        if (action == null) {
            return this;
        }
        return registerCallback(new RejectedPromise<E, V>(executor, exceptionClass) {
            @Override
            protected void handle(E exception) throws Throwable {
                action.rejected(exception);
                completeWithException(exception);
            }
        });
    }

    @Override
    public <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, Executor executor, final OnRejectedHandler<? super E, ? extends V> handler) {
        Arg.ensureNotNull(exceptionClass, "exceptionClass");
        Arg.ensureNotNull(executor, "executor");
        Arg.ensureNotNull(handler, "handler");
        return registerCallback(new RejectedPromise<E, V>(executor, exceptionClass) {
            @Override
            protected void handle(E exception) throws Throwable {
                handler.handle(exception);
                complete(handler.handle(exception));
            }
        });
    }

    @Override
    public <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, Executor executor, final OnRejectedHandler<? super E, ? extends Future<V>> fallback) {
        Arg.ensureNotNull(exceptionClass, "exceptionClass");
        Arg.ensureNotNull(executor, "executor");
        Arg.ensureNotNull(fallback, "fallback");
        return registerCallback(new RejectedPromise<E, V>(executor, exceptionClass) {
            @Override
            protected void handle(E exception) throws Throwable {
                Future<V> future = fallback.handle(exception);
                if (future != null) {
                    completeWithFuture(future);
                }
                else {
                    completeWithException(exception);
                }
            }
        });
    }

    @Override
    public Promise<V> whenCompleted(Executor executor, final OnCompleted<V> action) {
        Arg.ensureNotNull(action, "completed");
        Arg.ensureNotNull(executor, "executor");
        return registerCallback(new ComposedPromise<V, V>(executor) {
            @Override
            protected void completed(V result) throws Throwable {
                action.completed(AbstractPromise.this, result, null);
                complete(result);
            }

            @Override
            protected void completed(Throwable exception) throws Throwable {
                action.completed(AbstractPromise.this, null, exception);
                super.completed(exception);
            }
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return completeWithException(new CancellationException());
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        latch.await();
        if (state == PromiseState.REJECTED) {
            throw new ExecutionException(exception);
        }
        return result;
    }

    @Override
    public V get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!latch.await(timeout, timeUnit)) {
            throw new TimeoutException();
        }
        if (state == PromiseState.REJECTED) {
            throw new ExecutionException(exception);
        }
        return result;
    }

    @Override
    public V getNow(V defaultValue) throws ExecutionException, CancellationException {
        if (state == PromiseState.PENDING) {
            return defaultValue;
        }
        else if (state == PromiseState.RESOLVED) {
            return result;
        }
        else {
            if (exception instanceof CancellationException) {
                throw (CancellationException)exception;
            }
            throw new ExecutionException(exception);
        }
    }

    protected boolean complete(V result) {
        if (state != PromiseState.PENDING) {
            return false;
        }
        synchronized (lock) {
            if (state != PromiseState.PENDING) {
                return false;
            }
            state = PromiseState.RESOLVED;
            this.result = result;
            onResolved(result);
        }
        return true;
    }

    protected boolean completeWithException(Throwable exception) {
        if (state != PromiseState.PENDING) {
            return false;
        }
        synchronized (lock) {
            if (state != PromiseState.PENDING) {
                return false;
            }
            state = PromiseState.REJECTED;
            this.exception = exception;
            onRejected(exception);
        }
        return true;
    }

    protected void onResolved(V result) {
        onCompleted(PromiseState.RESOLVED, result, null);
    }

    protected void onRejected(Throwable exception) {
        onCompleted(PromiseState.REJECTED, null, exception);
    }

    protected void onCompleted(PromiseState state, V result, Throwable exception) {
        latch.countDown();
        invokeCallbacks(callbacks, result, exception);
    }

    private <V_OUT> Promise<V_OUT> registerCallback(final ComposedPromise<V, V_OUT> composedFuture) {
        registerCallback((OnCompleted<V>)composedFuture);
        return composedFuture;
    }

    protected void registerCallback(final OnCompleted<V> callback) {
        if (callback == null) {
            return;
        }
        if (state == PromiseState.PENDING) {
            synchronized (lock) {
                if (state == PromiseState.PENDING) {
                    callbacks.add(callback);
                    return;
                }
            }
        }
        invokeCallback(callback, result, exception);
    }

    private void invokeCallbacks(Iterable<OnCompleted<V>> callbacks, V result, Throwable exception) {
        for (OnCompleted<V> callback : callbacks) {
            invokeCallback(callback, result, exception);
        }
    }

    private void invokeCallback(final OnCompleted<V> callback, V result, Throwable exception) {
        try {
            callback.completed(this, result, exception);
        }
        catch (Throwable ignored) {
            //TODO: Should do something with this here
        }
    }

}
