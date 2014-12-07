package org.jpromise;

import org.jpromise.functions.*;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

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
        catch (InterruptedException exception) {
            throw new ExecutionException(exception);
        }
    }

    public Promise<V> then(OnResolved<? super V> action) {
        return this.then(action, CurrentThreadExecutor.INSTANCE);
    }

    public <V_APPLIED> Promise<V_APPLIED> thenApply(OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        return this.thenApply(function, CurrentThreadExecutor.INSTANCE);
    }

    public <V_COMPOSED> Promise<V_COMPOSED> thenCompose(OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        return this.thenCompose(function, CurrentThreadExecutor.INSTANCE);
    }

    public Promise<V> rejected(OnRejected<Throwable> action) {
        return this.rejected(Throwable.class, action, CurrentThreadExecutor.INSTANCE);
    }

    public <E extends Throwable> Promise<V> rejected(Class<E> exceptionClass, OnRejected<? super E> action) {
        return this.rejected(exceptionClass, action, CurrentThreadExecutor.INSTANCE);
    }

    public Promise<V> rejected(OnRejected<Throwable> action, Executor executor) {
        return this.rejected(Throwable.class, action, executor);
    }

    public Promise<V> handleWith(OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, handler, CurrentThreadExecutor.INSTANCE);
    }

    public Promise<V> handleWith(OnRejectedHandler<Throwable, ? extends V> handler, Executor executor) {
        return this.handleWith(Throwable.class, handler, executor);
    }

    public <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends V> handler) {
        return this.handleWith(exceptionClass, handler, CurrentThreadExecutor.INSTANCE);
    }

    public Promise<V> fallbackWith(OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, fallback, CurrentThreadExecutor.INSTANCE);
    }

    public Promise<V> fallbackWith(OnRejectedHandler<Throwable, ? extends Future<V>> fallback, Executor executor) {
        return this.fallbackWith(Throwable.class, fallback, executor);
    }

    public <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends Future<V>> fallback) {
        return this.fallbackWith(exceptionClass, fallback, CurrentThreadExecutor.INSTANCE);
    }

    public Promise<V> whenCompleted(OnCompleted<V> completed) {
        return this.whenCompleted(completed, CurrentThreadExecutor.INSTANCE);
    }

    public abstract Promise<V> then(OnResolved<? super V> action, Executor executor);
    public abstract <V_APPLIED> Promise<V_APPLIED> thenApply(OnResolvedFunction<? super V, ? extends V_APPLIED> function, Executor executor);
    public abstract <V_COMPOSED> Promise<V_COMPOSED> thenCompose(OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function, Executor executor);
    public abstract <E extends Throwable> Promise<V> rejected(Class<E> exceptionClass, OnRejected<? super E> action, Executor executor);
    public abstract <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends V> handler, Executor executor);
    public abstract <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends Future<V>> fallback, Executor executor);
    public abstract Promise<V> whenCompleted(OnCompleted<V> completed, Executor executor);

    public static <V> Promise<V> resolved(V result) {
        Deferred<V> deferred = new DeferredPromise<>();
        deferred.resolve(result);
        return deferred.promise();
    }

    public static <V> Promise<V> rejected(Throwable exception) {
        Deferred<V> deferred = new DeferredPromise<>();
        deferred.reject(exception);
        return deferred.promise();
    }
}
