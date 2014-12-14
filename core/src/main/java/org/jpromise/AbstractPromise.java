package org.jpromise;

import org.jpromise.functions.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public abstract class AbstractPromise<V> extends Promise<V> {
    private final Object lock = new Object();
    private final CountDownLatch latch = new CountDownLatch(1);
    private final List<Continuation<V>> callbacks = new LinkedList<>();
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
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        return registerCallback(new ContinuationPromise<V, V>(this, executor) {
            @Override
            protected void completeComposed(V result) throws Throwable {
                if (action != null) {
                    action.resolved(result);
                }
                complete(result);
            }
        });
    }

    @Override
    public <V_APPLIED> Promise<V_APPLIED> thenApply(Executor executor, final OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return registerCallback(new ContinuationPromise<V, V_APPLIED>(this, executor) {
            @Override
            protected void completeComposed(V result) throws Throwable {
                complete(function.resolved(result));
            }
        });
    }

    @Override
    public <V_COMPOSED> Promise<V_COMPOSED> thenCompose(Executor executor, final OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return registerCallback(new ContinuationPromise<V, V_COMPOSED>(this, executor) {
            @Override
            protected void completeComposed(V result) throws Throwable {
                completeWithFuture(function.resolved(result));
            }
        });
    }

    @Override
    public <E extends Throwable> Promise<V> whenRejected(Class<E> exceptionClass, Executor executor, final OnRejected<? super E> action) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        return registerCallback(new RejectionPromise<E, V>(this, executor, exceptionClass) {
            @Override
            protected void handle(E exception) throws Throwable {
                if (action != null) {
                    action.rejected(exception);
                }
                completeWithException(exception);
            }
        });
    }

    @Override
    public <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, final V result) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        return registerCallback(new RejectionPromise<E, V>(this, PromiseExecutors.CURRENT_THREAD, exceptionClass) {
            @Override
            protected void handle(E exception) throws Throwable {
                complete(result);
            }
        });
    }

    @Override
    public <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, Executor executor, final OnRejectedHandler<? super E, ? extends V> handler) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (handler == null) throw new IllegalArgumentException(mustNotBeNull("handler"));
        return registerCallback(new RejectionPromise<E, V>(this, executor, exceptionClass) {
            @Override
            protected void handle(E exception) throws Throwable {
                complete(handler.handle(exception));
            }
        });
    }

    @Override
    public <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, Executor executor, final OnRejectedHandler<? super E, ? extends Future<V>> fallback) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (fallback == null) throw new IllegalArgumentException(mustNotBeNull("fallback"));
        return registerCallback(new RejectionPromise<E, V>(this, executor, exceptionClass) {
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
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        return registerCallback(new ContinuationPromise<V, V>(this, executor) {
            @Override
            protected void completeComposed(V result) throws Throwable {
                action.completed(AbstractPromise.this, result, null);
                complete(result);
            }

            @Override
            protected void completeComposedWithException(Throwable exception) throws Throwable {
                action.completed(AbstractPromise.this, null, exception);
                super.completeComposedWithException(exception);
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

    protected boolean complete(V result) {
        if (state != PromiseState.PENDING) {
            return false;
        }
        synchronized (lock) {
            if (state != PromiseState.PENDING) return false;
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
            if (state != PromiseState.PENDING) return false;
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

    private <V_OUT> Promise<V_OUT> registerCallback(final ContinuationPromise<V, V_OUT> composedFuture) {
        registerCallback((Continuation<V>)composedFuture);
        return composedFuture;
    }

    private void registerCallback(final Continuation<V> callback) {
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

    private void invokeCallbacks(Iterable<Continuation<V>> callbacks, V result, Throwable exception) {
        for (Continuation<V> callback : callbacks) {
            invokeCallback(callback, result, exception);
        }
    }

    private void invokeCallback(final Continuation<V> callback, V result, Throwable exception) {
        callback.completed(this, result, exception);
    }
}
