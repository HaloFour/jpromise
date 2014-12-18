package org.jpromise;

import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnResolved;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

/**
 * Utility methods for creating new promises and synchronizing the results of multiple promises.
 */
public class PromiseManager {
    private PromiseManager() {
        throw new IllegalStateException();
    }

    /**
     * Creates a new promise for the specified operation that is scheduled on the default creation
     * {@link java.util.concurrent.Executor}.
     * @see org.jpromise.PromiseExecutors#DEFAULT_CREATION_EXECUTOR
     * @param runnable The operation that the promise represents.
     * @return A new {@link org.jpromise.Promise} representing the completion of the operation.
     */
    public static Promise<Void> create(Runnable runnable) {
        return create(PromiseExecutors.DEFAULT_CREATION_EXECUTOR, runnable);
    }

    /**
     * Creates a new promise for the specified operation that is scheduled on the default creation
     * {@link java.util.concurrent.Executor} and that will be resolved with the specified value
     * upon completion.
     * @param runnable The operation that the promise represents.
     * @param result The result that will be used to resolve the promise when the operation completes.
     * @param <V> The type of the promise result.
     * @return A new {@link org.jpromise.Promise} representing the completion of the operation.
     */
    public static <V> Promise<V> create(Runnable runnable, V result) {
        return create(PromiseExecutors.DEFAULT_CREATION_EXECUTOR, runnable, result);
    }

    /**
     * Creates a new promise for the specified operation that is scheduled on the default creation
     * {@link java.util.concurrent.Executor}.
     * @param callable The operation that the promise represents.
     * @param <V> The return type of the {@link java.util.concurrent.Callable} operation.
     * @return A new {@link org.jpromise.Promise} representing the completion of the operation.
     */
    public static <V> Promise<V> create(Callable<V> callable) {
        return create(PromiseExecutors.DEFAULT_CREATION_EXECUTOR, callable);
    }

    /**
     * Creates a new promise for the specified operation that is scheduled on the specified
     * {@link java.util.concurrent.Executor}.
     * @param executor The {@link java.util.concurrent.Executor} on which the operation is scheduled.
     * @param runnable The operation that the promise represents.
     * @return A new {@link org.jpromise.Promise} representing the completion of the operation.
     */
    public static Promise<Void> create(Executor executor, Runnable runnable) {
        return create(executor, runnable, null);
    }

    /**
     * Creates a new promise for the specified operation that is scheduled on the specified
     * {@link java.util.concurrent.Executor} and that will be resolved with the specified value
     * upon completion.
     * @param executor The {@link java.util.concurrent.Executor} on which the operation is scheduled.
     * @param runnable The operation that the promise represents.
     * @param result The result that will be used to resolve the promise when the operation completes.
     * @param <V> The type of the promise result.
     * @return A new {@link org.jpromise.Promise} representing the completion of the operation.
     */
    public static <V> Promise<V> create(Executor executor, final Runnable runnable, final V result) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (runnable == null) throw new IllegalArgumentException(mustNotBeNull("runnable"));
        final Deferred<V> deferred = Promise.defer();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                    deferred.resolve(result);
                }
                catch (Throwable exception) {
                    deferred.reject(exception);
                }
            }
        });
        return deferred.promise();
    }

    /**
     * Creates a new promise for the specified operation that is scheduled on the specified
     * {@link java.util.concurrent.Executor}.
     * @param executor The {@link java.util.concurrent.Executor} on which the operation is scheduled.
     * @param callable The operation that the promise represents.
     * @param <V> The return type of the {@link java.util.concurrent.Callable} operation.
     * @return A new {@link org.jpromise.Promise} representing the completion of the operation.
     */
    public static <V> Promise<V> create(Executor executor, final Callable<V> callable) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (callable == null) throw new IllegalArgumentException(mustNotBeNull("callable"));
        final Deferred<V> deferred = Promise.defer();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deferred.resolve(callable.call());
                }
                catch (Throwable exception) {
                    deferred.reject(exception);
                }
            }
        });
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

    private static <V> void whenCompleted(Promise<V> promise, Executor executor, OnCompleted<V> action, final Deferred<Void> deferred, final AtomicInteger counter) {
        if (action != null) {
            promise = promise.whenCompleted(executor, action);
        }
        promise.whenCompleted(new OnCompleted<V>() {
            @Override
            public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                if (counter.decrementAndGet() <= 0) {
                    deferred.resolve(null);
                }
            }
        });
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the completion of all of the specified promises.  Any of
     * the specified promises that are rejected are counted towards the completed promises.
     * @param promises The array of promises.
     * @return A new {@link org.jpromise.Promise} that represents the completion of all of the specified {@code promises}.
     */
    public static Promise<Void> whenAllCompleted(Promise<?>... promises) {
        if (promises == null || promises.length == 0) {
            return Promise.resolved(null);
        }
        return whenAllCompleted(Arrays.asList(promises));
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the completion of all of the specified promises.  Any of
     * the specified promises that are rejected are counted towards the completed promises.
     * @param promises The collection of promises.
     * @return A new {@link org.jpromise.Promise} that represents the completion of all of the specified {@code promises}.
     */
    @SuppressWarnings("unchecked")
    public static Promise<Void> whenAllCompleted(Iterable<? extends Promise<?>> promises) {
        return whenAllCompletedImpl((Iterable)promises, null, null);
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the completion of all of the specified promises after the
     * specified action is performed.
     * @param promises The collection of promises.
     * @param action The action to perform with the result of each of the promises as they are completed.
     * @param <V> The result type of the promises.
     * @return A new {@link org.jpromise.Promise} that represents the completion of all of the specified {@code promises}
     * after the specified action is performed.
     */
    public static <V> Promise<Void> whenAllCompleted(Iterable<? extends Promise<V>> promises, OnCompleted<V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        return whenAllCompletedImpl(promises, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the completion of all of the specified promises after the
     * specified action is performed using the specified {@link java.util.concurrent.Executor}.
     * @param promises The collection of promises.
     * @param executor The {@link java.util.concurrent.Executor} on which to schedule the {@code action}.
     * @param action The action to perform with the result of each of the promises as they are completed.
     * @param <V> The result type of the promises.
     * @return A new {@link org.jpromise.Promise} that represents the completion of all of the specified {@code promises}
     * after the specified action is performed.
     */
    public static <V> Promise<Void> whenAllCompleted(Iterable<? extends Promise<V>> promises, Executor executor, OnCompleted<V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        return whenAllCompletedImpl(promises, executor, action);
    }

    private static <V> Promise<Void> whenAllCompletedImpl(Iterable<? extends Promise<V>> promises, Executor executor, OnCompleted<V> action) {
        if (promises == null) {
            return Promise.resolved(null);
        }

        final Deferred<Void> deferred = Promise.defer();
        final AtomicInteger counter = new AtomicInteger();

        counter.incrementAndGet();
        for (final Promise<V> promise : promises) {
            if (promise == null) {
                continue;
            }
            counter.incrementAndGet();
            whenCompleted(promise, executor, action, deferred, counter);
        }
        if (counter.decrementAndGet() <= 0) {
            deferred.resolve(null);
        }

        return deferred.promise();
    }

    private static <V> void whenResolved(Promise<V> promise, Executor executor, OnResolved<? super V> action, final Deferred<Void> deferred, final AtomicInteger counter, final AtomicBoolean done) {
        if (action != null) {
            promise = promise.then(executor, action);
        }
        promise.whenCompleted(new OnCompleted<V>() {
            @Override
            public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                if (promise.isRejected()) {
                    if (done.compareAndSet(false, true)) {
                        deferred.reject(exception);
                    }
                }
                else {
                    if (counter.decrementAndGet() <= 0 && done.compareAndSet(false, true)) {
                        deferred.resolve(null);
                    }
                }
            }
        });
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the successful resolution of the specified promises.  If any
     * of the specified promises is rejected then the returned promise immediately rejects with the same exception.
     * @param promises The array of promises.
     * @return A new {@link org.jpromise.Promise} that represents the resolution of all of the specified {@code promises}.
     */
    public static Promise<Void> whenAllResolved(Promise<?>... promises) {
        if (promises == null || promises.length == 0) {
            return Promise.resolved(null);
        }
        return whenAllResolved(Arrays.asList(promises));
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the successful resolution of the specified promises.  If any
     * of the specified promises is rejected then the returned promise immediately rejects with the same exception.
     * @param promises The collection of promises.
     * @return A new {@link org.jpromise.Promise} that represents the resolution of all of the specified {@code promises}.
     */
    @SuppressWarnings("unchecked")
    public static Promise<Void> whenAllResolved(Iterable<? extends Promise<?>> promises) {
        return whenAllResolvedImpl((Iterable)promises, null, null);
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the successful resolution of the specified promises after
     * the specified action is performed.  If any of the specified promises is rejected then the returned promise
     * immediately rejects with the same exception.
     * @param promises The collection of promises.
     * @param action The action to perform with the result of each of the promises as they are resolved.
     * @param <V> The result type of the promises.
     * @return A new {@link org.jpromise.Promise} that represents the resolution of all of the specified {@code promises}
     * after the specified action is performed.
     */
    public static <V> Promise<Void> whenAllResolved(Iterable<? extends Promise<V>> promises, OnResolved<? super V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        return whenAllResolvedImpl(promises, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }
    /**
     * Returns a {@link org.jpromise.Promise} representing the successful resolution of the specified promises after
     * the specified action is performed using the specified {@link java.util.concurrent.Executor}.  If any of the
     * specified promises is rejected then the returned promise immediately rejects with the same exception.
     * @param promises The collection of promises.
     * @param executor The {@link java.util.concurrent.Executor} on which to schedule the {@code action}.
     * @param action The action to perform with the result of each of the promises as they are resolved.
     * @param <V> The result type of the promises.
     * @return A new {@link org.jpromise.Promise} that represents the resolution of all of the specified {@code promises}
     * after the specified action is performed.
     */
    public static <V> Promise<Void> whenAllResolved(Iterable<? extends Promise<V>> promises, Executor executor, OnResolved<? super V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        return whenAllResolvedImpl(promises, executor, action);
    }

    private static <V> Promise<Void> whenAllResolvedImpl(Iterable<? extends Promise<V>> promises, Executor executor, OnResolved<? super V> action) {
        if (promises == null) {
            return Promise.resolved(null);
        }

        final Deferred<Void> deferred = Promise.defer();
        final AtomicInteger counter = new AtomicInteger();
        final AtomicBoolean done = new AtomicBoolean();

        counter.incrementAndGet();
        for (final Promise<V> promise : promises) {
            if (promise == null) {
                continue;
            }
            counter.incrementAndGet();
            whenResolved(promise, executor, action, deferred, counter, done);
        }
        if (counter.decrementAndGet() <= 0 && done.compareAndSet(false, true)) {
            deferred.resolve(null);
        }

        return deferred.promise();
    }

    public static <V> Promise<V> whenAnyCompleted(Promise<V> promise1, Promise<V> promise2) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(2);
        promises.add(promise1);
        promises.add(promise2);
        return whenAnyCompleted(promises);
    }

    public static <V> Promise<V> whenAnyCompleted(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(3);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        return whenAnyCompleted(promises);
    }

    public static <V> Promise<V> whenAnyCompleted(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(4);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);
        return whenAnyCompleted(promises);
    }

    public static <V> Promise<V> whenAnyCompleted(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4, Promise<V> promise5) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(5);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);
        promises.add(promise5);
        return whenAnyCompleted(promises);
    }

    public static <V> Promise<V> whenAnyCompleted(Promise<V>[] promises) {
        if (promises == null || promises.length == 0) {
            Deferred<V> deferred = Promise.defer();
            return deferred.promise();
        }
        return whenAnyCompleted(Arrays.asList(promises));
    }

    public static <V> Promise<V> whenAnyCompleted(Iterable<? extends Promise<V>> promises) {
        final Deferred<V> deferred = Promise.defer();
        if (promises == null) {
            return deferred.promise();
        }

        final AtomicBoolean done = new AtomicBoolean();
        for (Promise<V> promise : promises) {
            if (promise == null) {
                continue;
            }
            promise.whenCompleted(new OnCompleted<V>() {
                @Override
                public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                    if (promise.isDone() && done.compareAndSet(false, true)) {
                        switch (promise.state()) {
                            case RESOLVED:
                                deferred.resolve(result);
                                break;
                            case REJECTED:
                                deferred.reject(exception);
                                break;
                        }
                    }
                }
            });
        }

        return deferred.promise();
    }

    public static <V> Promise<V> whenAnyResolved(Promise<V> promise1, Promise<V> promise2) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(2);
        promises.add(promise1);
        promises.add(promise2);
        return whenAnyResolved(promises);
    }

    public static <V> Promise<V> whenAnyResolved(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(3);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        return whenAnyResolved(promises);
    }

    public static <V> Promise<V> whenAnyResolved(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(4);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);
        return whenAnyResolved(promises);
    }

    public static <V> Promise<V> whenAnyResolved(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4, Promise<V> promise5) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(5);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);
        promises.add(promise5);
        return whenAnyResolved(promises);
    }

    public static <V> Promise<V> whenAnyResolved(Promise<V>[] promises) {
        if (promises == null || promises.length == 0) {
            Deferred<V> deferred = Promise.defer();
            return deferred.promise();
        }
        return whenAnyResolved(Arrays.asList(promises));
    }

    public static <V> Promise<V> whenAnyResolved(Iterable<? extends Promise<V>> promises) {
        final Deferred<V> deferred = Promise.defer();
        if (promises == null) {
            return deferred.promise();
        }

        final AtomicBoolean done = new AtomicBoolean();
        for (Promise<V> promise : promises) {
            if (promise == null) {
                continue;
            }
            promise.whenCompleted(new OnCompleted<V>() {
                @Override
                public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                    if (promise.isResolved() && done.compareAndSet(false, true)) {
                        deferred.resolve(result);
                    }
                }
            });
        }

        return deferred.promise();
    }
}
