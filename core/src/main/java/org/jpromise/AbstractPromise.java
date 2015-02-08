package org.jpromise;

import org.jpromise.functions.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public abstract class AbstractPromise<V> implements Promise<V> {
    private final Object lock = new Object();
    private final CountDownLatch latch = new CountDownLatch(1);
    private final List<Continuation<V>> callbacks = new LinkedList<Continuation<V>>();
    private PromiseState state = PromiseState.PENDING;
    private V result;
    private Throwable exception;

    @Override
    public PromiseState state() {
        return state;
    }

    @Override
    public boolean isDone() {
        return this.state() != PromiseState.PENDING;
    }

    @Override
    public boolean isFulfilled() {
        return this.state() == PromiseState.FULFILLED;
    }

    @Override
    public boolean isRejected() {
        return this.state() == PromiseState.REJECTED;
    }

    @Override
    public boolean isCancelled() {
        return (exception instanceof CancellationException);
    }

    @Override
    public String toString() {
        switch (state) {
            case FULFILLED:
                return String.format("[FULFILLED]: %s", result);
            case REJECTED:
                return String.format("[REJECTED]: %s", exception);
            default:
                return "[PENDING]";
        }
    }

    @Override
    public Promise<V> then(OnFulfilled<? super V> action) {
        return this.then(PromiseExecutors.getContextExecutor(), action);
    }

    @Override
    public Promise<V> then(Executor executor, final OnFulfilled<? super V> action) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        return registerCallback(new ContinuationPromise<V, V>(this, executor) {
            @Override
            protected void completeComposed(V result) throws Throwable {
                if (action != null) {
                    action.fulfilled(result);
                }
                complete(result);
            }
        });
    }

    @Override
    public <V_APPLIED> Promise<V_APPLIED> thenApply(OnFulfilledFunction<? super V, ? extends V_APPLIED> function) {
        return this.thenApply(PromiseExecutors.getContextExecutor(), function);
    }

    @Override
    public <V_APPLIED> Promise<V_APPLIED> thenApply(Executor executor, final OnFulfilledFunction<? super V, ? extends V_APPLIED> function) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return registerCallback(new ContinuationPromise<V, V_APPLIED>(this, executor) {
            @Override
            protected void completeComposed(V result) throws Throwable {
                complete(function.fulfilled(result));
            }
        });
    }

    @Override
    public <V_COMPOSED> Promise<V_COMPOSED> thenCompose(OnFulfilledFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        return this.thenCompose(PromiseExecutors.getContextExecutor(), function);
    }

    @Override
    public <V_COMPOSED> Promise<V_COMPOSED> thenCompose(Executor executor, final OnFulfilledFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return registerCallback(new ContinuationPromise<V, V_COMPOSED>(this, executor) {
            @Override
            protected void completeComposed(V result) throws Throwable {
                completeWithFuture(function.fulfilled(result));
            }
        });
    }

    @Override
    public Promise<V> whenRejected(OnRejected<Throwable> action) {
        return this.whenRejected(Throwable.class, PromiseExecutors.getContextExecutor(), action);
    }

    @Override
    public <E extends Throwable> Promise<V> whenRejected(Class<E> exceptionClass, OnRejected<? super E> action) {
        return this.whenRejected(exceptionClass, PromiseExecutors.getContextExecutor(), action);
    }

    @Override
    public Promise<V> whenRejected(Executor executor, OnRejected<Throwable> action) {
        return this.whenRejected(Throwable.class, executor, action);
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
    public Promise<V> handleWith(V result) {
        return this.handleWith(Throwable.class, result);
    }

    @Override
    public Promise<V> handleWith(OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, PromiseExecutors.getContextExecutor(), handler);
    }

    @Override
    public Promise<V> handleWith(Executor executor, OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, executor, handler);
    }

    @Override
    public <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends V> handler) {
        return this.handleWith(exceptionClass, PromiseExecutors.getContextExecutor(), handler);
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
    public Promise<V> fallbackWith(OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, PromiseExecutors.getContextExecutor(), fallback);
    }

    @Override
    public Promise<V> fallbackWith(Executor executor, OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, executor, fallback);
    }

    @Override
    public <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends Future<V>> fallback) {
        return this.fallbackWith(exceptionClass, PromiseExecutors.getContextExecutor(), fallback);
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
    public Promise<V> whenCompleted(OnCompleted<V> action) {
        return this.whenCompleted(PromiseExecutors.getContextExecutor(), action);
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
    public Promise<V> cancelAfter(final boolean mayInterruptIfRunning, long timeout, TimeUnit timeUnit) {
        if (isDone()) {
            return this;
        }
        final Timer timer = new Timer(true);
        final AtomicBoolean flag = new AtomicBoolean();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (flag.compareAndSet(false, true)) {
                    AbstractPromise.this.cancel(mayInterruptIfRunning);
                }
            }
        }, timeUnit.toMillis(timeout));
        this.whenCompleted(new OnCompleted<V>() {
            @Override
            public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                if (flag.compareAndSet(false, true)) {
                    timer.cancel();
                }
            }
        });
        return this;
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
        if (!isDone()) {
            return defaultValue;
        }
        try {
            return this.get();
        }
        catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof CancellationException) {
                throw (CancellationException)cause;
            }
            throw exception;
        }
        catch (InterruptedException exception) {
            throw new ExecutionException(exception);
        }
    }

    protected boolean complete(V result) {
        if (state != PromiseState.PENDING) {
            return false;
        }
        synchronized (lock) {
            if (state != PromiseState.PENDING) return false;
            state = PromiseState.FULFILLED;
            this.result = result;
            onFulfilled(result);
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

    protected void onFulfilled(V result) {
        onCompleted(PromiseState.FULFILLED, result, null);
    }

    protected void onRejected(Throwable exception) {
        onCompleted(PromiseState.REJECTED, null, exception);
    }

    protected void onCompleted(PromiseState state, V result, Throwable exception) {
        latch.countDown();
        invokeCallbacks(callbacks, result, exception);
    }

    <V_OUT> Promise<V_OUT> registerCallback(final ContinuationPromise<V, V_OUT> composedFuture) {
        registerCallback((Continuation<V>)composedFuture);
        return composedFuture;
    }

    private void registerCallback(final Continuation<V> callback) {
        boolean invokeImmediately = true;
        if (state == PromiseState.PENDING) {
            synchronized (lock) {
                if (state == PromiseState.PENDING) {
                    invokeImmediately = false;
                    callbacks.add(callback);
                }
            }
        }
        if (invokeImmediately) {
            invokeCallback(callback, result, exception);
        }
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
