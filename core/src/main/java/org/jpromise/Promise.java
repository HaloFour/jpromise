package org.jpromise;

import org.jpromise.functions.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Promise<V> implements Future<V> {
    public abstract PromiseState state();

    public boolean isDone() {
        return this.state() != PromiseState.PENDING;
    }

    public boolean isResolved() {
        return this.state() == PromiseState.RESOLVED;
    }

    public boolean isRejected() {
        return this.state() == PromiseState.REJECTED;
    }

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

    public Promise<Boolean> cancelAfter(final boolean mayInterruptIfRunning, long timeout, TimeUnit timeUnit) {
        if (isDone()) {
            return Promise.resolved(false);
        }
        final Deferred<Boolean> deferred = Promise.defer();
        final Timer timer = new Timer(true);
        final AtomicBoolean flag = new AtomicBoolean();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (flag.compareAndSet(false, true)) {
                    deferred.resolve(Promise.this.cancel(mayInterruptIfRunning));
                }
            }
        }, timeUnit.toMillis(timeout));
        this.whenCompleted(new OnCompleted<V>() {
            @Override
            public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                if (flag.compareAndSet(false, true)) {
                    timer.cancel();
                    deferred.resolve(false);
                }
            }
        });
        return deferred.promise();
    }

    public Promise<V> then(OnResolved<? super V> action) {
        return this.then(PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    public <V_APPLIED> Promise<V_APPLIED> thenApply(OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        return this.thenApply(PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, function);
    }

    public <V_COMPOSED> Promise<V_COMPOSED> thenCompose(OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        return this.thenCompose(PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, function);
    }

    public Promise<V> rejected(OnRejected<Throwable> action) {
        return this.rejected(Throwable.class, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    public <E extends Throwable> Promise<V> rejected(Class<E> exceptionClass, OnRejected<? super E> action) {
        return this.rejected(exceptionClass, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    public Promise<V> rejected(Executor executor, OnRejected<Throwable> action) {
        return this.rejected(Throwable.class, executor, action);
    }

    public Promise<V> handleWith(V result) {
        return this.handleWith(Throwable.class, result);
    }

    public Promise<V> handleWith(OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, handler);
    }

    public Promise<V> handleWith(Executor executor, OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, executor, handler);
    }

    public <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends V> handler) {
        return this.handleWith(exceptionClass, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, handler);
    }

    public Promise<V> fallbackWith(OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, fallback);
    }

    public Promise<V> fallbackWith(Executor executor, OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, executor, fallback);
    }

    public <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends Future<V>> fallback) {
        return this.fallbackWith(exceptionClass, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, fallback);
    }

    public Promise<V> whenCompleted(OnCompleted<V> action) {
        return this.whenCompleted(PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    public abstract Promise<V> then(Executor executor, OnResolved<? super V> action);
    public abstract <V_APPLIED> Promise<V_APPLIED> thenApply(Executor executor, OnResolvedFunction<? super V, ? extends V_APPLIED> function);
    public abstract <V_COMPOSED> Promise<V_COMPOSED> thenCompose(Executor executor, OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function);
    public abstract <E extends Throwable> Promise<V> rejected(Class<E> exceptionClass, Executor executor, OnRejected<? super E> action);
    public abstract <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, V result);
    public abstract <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, Executor executor, OnRejectedHandler<? super E, ? extends V> handler);
    public abstract <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, Executor executor, OnRejectedHandler<? super E, ? extends Future<V>> fallback);
    public abstract Promise<V> whenCompleted(Executor executor, OnCompleted<V> action);

    public static <V> Deferred<V> defer() {
        return new DeferredPromise<>();
    }

    public static Promise<Void> resolved() {
        return resolved(null);
    }

    public static <V> Promise<V> resolved(V result) {
        Deferred<V> deferred = defer();
        deferred.resolve(result);
        return deferred.promise();
    }

    public static <V> Promise<V> rejected(Class<V> resultClass, Throwable exception) {
        return rejected(exception);
    }

    public static <V> Promise<V> rejected(Throwable exception) {
        Deferred<V> deferred = defer();
        deferred.reject(exception);
        return deferred.promise();
    }
}
