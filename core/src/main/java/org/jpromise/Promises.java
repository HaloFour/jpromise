package org.jpromise;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

/**
 * Provides helper methods for creating promises.
 */
public class Promises {
    private Promises() {
        throw new IllegalStateException();
    }

    /**
     * Creates a new {@link org.jpromise.Deferred} operation that can be used to generate a {@link org.jpromise.Promise}
     * that is eventually completed.
     * @param <V> The type of the result of the promise.
     * @return The deferred operation.
     */
    public static <V> Deferred<V> defer() {
        return new DeferredPromise<V>();
    }

    /**
     * Creates a new {@link org.jpromise.Deferred} operation that can be used to generate a {@link org.jpromise.Promise}
     * that is eventually completed.  This method can be used to aid in generic type inference with the Java compiler.
     * @param resultClass The class representing the result type of the promise.
     * @param <V> The type of the result of the promise.
     * @return The deferred operation.
     */
    public static <V> Deferred<V> defer(Class<V> resultClass) {
        return new DeferredPromise<V>();
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that is already fulfilled.
     * @return A fulfilled promise.
     */
    public static Promise<Void> fulfilled() {
        return fulfilled(null);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that is already fulfilled with the specified result.
     * @param result The result of the returned promise.
     * @param <V> The type of the result of the promise.
     * @return A fulfilled promise.
     */
    public static <V> Promise<V> fulfilled(V result) {
        Deferred<V> deferred = defer();
        deferred.fulfill(result);
        return deferred.promise();
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that is already rejected with the specified exception.
     * @param exception The exception of the returned rejected promise.
     * @param <V> The type of the result of the promise.
     * @return A rejected promise.
     */
    public static <V> Promise<V> rejected(Throwable exception) {
        Deferred<V> deferred = defer();
        deferred.reject(exception);
        return deferred.promise();
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that is already rejected with the specified exception.  This
     * method can be used to aid in generic type inference with the Java compiler.
     * @param resultClass The class of the result of the promise.
     * @param exception The exception of the returned rejected promise.
     * @param <V> The type of the result of the promise.
     * @return A rejected promise.
     */
    public static <V> Promise<V> rejected(Class<V> resultClass, Throwable exception) {
        return rejected(exception);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that will be fulfilled when the timeout expires.
     * @param timeout The amount of time to wait.
     * @param timeUnit The time unit of the {@code timeout} argument.
     * @return A promise that will be fulfilled when the timeout expires.
     */
    public static Promise<Void> delay(long timeout, TimeUnit timeUnit) {
        if (timeUnit == null) throw new IllegalArgumentException(mustNotBeNull("timeUnit"));
        Timer timer = new Timer();
        final Deferred<Void> deferred = Promises.defer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                deferred.fulfill(null);
            }
        }, timeUnit.toMillis(timeout));
        return deferred.promise();
    }

    /**
     * Creates a promise representing the completion of the specified {@link java.util.concurrent.Future} instance.
     * @param future The {@link java.util.concurrent.Future} instance to be converted into a {@link org.jpromise.Promise}.
     * @param <V> The result type of the future.
     * @return A new {@link org.jpromise.Promise} representing the completion of the specified {@code future}.
     */
    public static <V> Promise<V> fromFuture(Future<V> future) {
        return fromFuture(PromiseExecutors.DEFAULT_FUTURE_EXECUTOR, future);
    }

    /**
     * Creates a promise representing the completion of the specified {@link java.util.concurrent.Future} instance
     * using the specified {@link java.util.concurrent.Executor} to block on the operation if it is not already
     * completed.
     * @param executor The {@link java.util.concurrent.Executor} which will be used to block on the {@code future}
     *                 until it is completed.
     * @param future The {@link java.util.concurrent.Future} instance to be converted into a {@link org.jpromise.Promise}.
     * @param <V> The result type of the future.
     * @return A new {@link org.jpromise.Promise} representing the completion of the specified {@code future}.
     */
    public static <V> Promise<V> fromFuture(Executor executor, Future<V> future) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (future == null) throw new IllegalArgumentException(mustNotBeNull("future"));
        if (future instanceof Promise) {
            return (Promise<V>)future;
        }
        return new FuturePromise<V>(executor, future);
    }

    /**
     * Creates a promise representing the completion of the specified {@link java.util.concurrent.Future} instance
     * that will wait for up until the specified duration until its completion.
     * @param future The {@link java.util.concurrent.Future} instance to be converted into a {@link org.jpromise.Promise}.
     * @param timeout The maximum amount of time to wait for the {@code future} to complete.
     * @param timeUnit The unit of time for the {@code timeout} argument.
     * @param <V> The result type of the future.
     * @return A new {@link org.jpromise.Promise} representing the completion of the specified {@code future}.
     */
    public static <V> Promise<V> fromFuture(Future<V> future, long timeout, TimeUnit timeUnit) {
        return fromFuture(PromiseExecutors.DEFAULT_FUTURE_EXECUTOR, future, timeout, timeUnit);
    }

    /**
     * Creates a promise representing the completion of the specified {@link java.util.concurrent.Future} instance
     * that will wait for up until the specified duration until its completion using the specified
     * {@link java.util.concurrent.Executor} to block on the operation if it is not already completed.
     * @param executor The {@link java.util.concurrent.Executor} which will be used to block on the {@code future}
     *                 until it is completed.
     * @param future The {@link java.util.concurrent.Future} instance to be converted into a {@link org.jpromise.Promise}.
     * @param timeout The maximum amount of time to wait for the {@code future} to complete.
     * @param timeUnit The unit of time for the {@code timeout} argument.
     * @param <V> The result type of the future.
     * @return A new {@link org.jpromise.Promise} representing the completion of the specified {@code future}.
     */
    public static <V> Promise<V> fromFuture(Executor executor, Future<V> future, long timeout, TimeUnit timeUnit) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (future == null) throw new IllegalArgumentException(mustNotBeNull("future"));
        if (timeUnit == null) throw new IllegalArgumentException(mustNotBeNull("timeUnit"));
        if (future instanceof Promise) {
            Promise<V> promise = (Promise<V>)future;
            promise.cancelAfter(true, timeout, timeUnit);
            return promise;
        }
        return new FuturePromise<V>(executor, future, timeout, timeUnit);
    }
}
