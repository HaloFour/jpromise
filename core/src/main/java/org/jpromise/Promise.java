package org.jpromise;

import org.jpromise.functions.*;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public abstract class Promise<V> implements Future<V> {
    public static final Executor DEFAULT_EXECUTOR = CurrentThreadExecutor.INSTANCE;

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
        return this.then(DEFAULT_EXECUTOR, action);
    }

    public <V_APPLIED> Promise<V_APPLIED> thenApply(OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        return this.thenApply(DEFAULT_EXECUTOR, function);
    }

    public <V_COMPOSED> Promise<V_COMPOSED> thenCompose(OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        return this.thenCompose(DEFAULT_EXECUTOR, function);
    }

    public Promise<V> rejected(OnRejected<Throwable> action) {
        return this.rejected(Throwable.class, DEFAULT_EXECUTOR, action);
    }

    public <E extends Throwable> Promise<V> rejected(Class<E> exceptionClass, OnRejected<? super E> action) {
        return this.rejected(exceptionClass, DEFAULT_EXECUTOR, action);
    }

    public Promise<V> rejected(Executor executor, OnRejected<Throwable> action) {
        return this.rejected(Throwable.class, executor, action);
    }

    public Promise<V> handleWith(OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, DEFAULT_EXECUTOR, handler);
    }

    public Promise<V> handleWith(Executor executor, OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, executor, handler);
    }

    public <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends V> handler) {
        return this.handleWith(exceptionClass, DEFAULT_EXECUTOR, handler);
    }

    public Promise<V> fallbackWith(OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, DEFAULT_EXECUTOR, fallback);
    }

    public Promise<V> fallbackWith(Executor executor, OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, executor, fallback);
    }

    public <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends Future<V>> fallback) {
        return this.fallbackWith(exceptionClass, DEFAULT_EXECUTOR, fallback);
    }

    public Promise<V> whenCompleted(OnCompleted<V> action) {
        return this.whenCompleted(DEFAULT_EXECUTOR, action);
    }

    public abstract Promise<V> then(Executor executor, OnResolved<? super V> action);
    public abstract <V_APPLIED> Promise<V_APPLIED> thenApply(Executor executor, OnResolvedFunction<? super V, ? extends V_APPLIED> function);
    public abstract <V_COMPOSED> Promise<V_COMPOSED> thenCompose(Executor executor, OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function);
    public abstract <E extends Throwable> Promise<V> rejected(Class<E> exceptionClass, Executor executor, OnRejected<? super E> action);
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
