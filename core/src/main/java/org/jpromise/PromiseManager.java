package org.jpromise;

import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnFulfilled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
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

    private static <V> void whenCompleted(Promise<V> promise, Executor executor, OnCompleted<V> action, final Deferred<Void> deferred, final AtomicInteger counter) {
        if (action != null) {
            promise = promise.whenCompleted(executor, action);
        }
        promise.whenCompleted(new OnCompleted<V>() {
            @Override
            public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                if (counter.decrementAndGet() <= 0) {
                    deferred.fulfill(null);
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
            return Promises.fulfilled(null);
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
            return Promises.fulfilled(null);
        }

        final Deferred<Void> deferred = Promises.defer();
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
            deferred.fulfill(null);
        }

        return deferred.promise();
    }

    private static <V> void whenFulfilled(Promise<V> promise, Executor executor, OnFulfilled<? super V> action, final Deferred<Void> deferred, final AtomicInteger counter, final AtomicBoolean done) {
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
                        deferred.fulfill(null);
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
    public static Promise<Void> whenAllFulfilled(Promise<?>... promises) {
        if (promises == null || promises.length == 0) {
            return Promises.fulfilled(null);
        }
        return whenAllFulfilled(Arrays.asList(promises));
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the successful resolution of the specified promises.  If any
     * of the specified promises is rejected then the returned promise immediately rejects with the same exception.
     * @param promises The collection of promises.
     * @return A new {@link org.jpromise.Promise} that represents the resolution of all of the specified {@code promises}.
     */
    @SuppressWarnings("unchecked")
    public static Promise<Void> whenAllFulfilled(Iterable<? extends Promise<?>> promises) {
        return whenAllFulfilledImpl((Iterable) promises, null, null);
    }

    /**
     * Returns a {@link org.jpromise.Promise} representing the successful resolution of the specified promises after
     * the specified action is performed.  If any of the specified promises is rejected then the returned promise
     * immediately rejects with the same exception.
     * @param promises The collection of promises.
     * @param action The action to perform with the result of each of the promises as they are fulfilled.
     * @param <V> The result type of the promises.
     * @return A new {@link org.jpromise.Promise} that represents the resolution of all of the specified {@code promises}
     * after the specified action is performed.
     */
    public static <V> Promise<Void> whenAllFulfilled(Iterable<? extends Promise<V>> promises, OnFulfilled<? super V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        return whenAllFulfilledImpl(promises, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }
    /**
     * Returns a {@link org.jpromise.Promise} representing the successful resolution of the specified promises after
     * the specified action is performed using the specified {@link java.util.concurrent.Executor}.  If any of the
     * specified promises is rejected then the returned promise immediately rejects with the same exception.
     * @param promises The collection of promises.
     * @param executor The {@link java.util.concurrent.Executor} on which to schedule the {@code action}.
     * @param action The action to perform with the result of each of the promises as they are fulfilled.
     * @param <V> The result type of the promises.
     * @return A new {@link org.jpromise.Promise} that represents the resolution of all of the specified {@code promises}
     * after the specified action is performed.
     */
    public static <V> Promise<Void> whenAllFulfilled(Iterable<? extends Promise<V>> promises, Executor executor, OnFulfilled<? super V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        return whenAllFulfilledImpl(promises, executor, action);
    }

    private static <V> Promise<Void> whenAllFulfilledImpl(Iterable<? extends Promise<V>> promises, Executor executor, OnFulfilled<? super V> action) {
        if (promises == null) {
            return Promises.fulfilled(null);
        }

        final Deferred<Void> deferred = Promises.defer();
        final AtomicInteger counter = new AtomicInteger();
        final AtomicBoolean done = new AtomicBoolean();

        counter.incrementAndGet();
        for (final Promise<V> promise : promises) {
            if (promise == null) {
                continue;
            }
            counter.incrementAndGet();
            whenFulfilled(promise, executor, action, deferred, counter, done);
        }
        if (counter.decrementAndGet() <= 0 && done.compareAndSet(false, true)) {
            deferred.fulfill(null);
        }

        return deferred.promise();
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed propagating the result or rejection of the first
     * of the specified promises that are completed.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the completion of the first of the completed promises.
     */
    public static <V> Promise<V> whenAnyCompleted(Promise<V> promise1, Promise<V> promise2) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(2);
        promises.add(promise1);
        promises.add(promise2);
        return whenAnyCompleted(promises);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed propagating the result or rejection of the first
     * of the specified promises that are completed.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param promise3 The third promise.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the completion of the first of the completed promises.
     */
    public static <V> Promise<V> whenAnyCompleted(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(3);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        return whenAnyCompleted(promises);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed propagating the result or rejection of the first
     * of the specified promises that are completed.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param promise3 The third promise.
     * @param promise4 The fourth promise.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the completion of the first of the completed promises.
     */
    public static <V> Promise<V> whenAnyCompleted(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(4);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);
        return whenAnyCompleted(promises);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed propagating the result or rejection of the first
     * of the specified promises that are completed.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param promise3 The third promise.
     * @param promise4 The fourth promise.
     * @param promise5 The fifth promise.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the completion of the first of the completed promises.
     */
    public static <V> Promise<V> whenAnyCompleted(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4, Promise<V> promise5) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(5);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);
        promises.add(promise5);
        return whenAnyCompleted(promises);
    }
    /**
     * Returns a new {@link org.jpromise.Promise} which is completed propagating the result or rejection of the first
     * of the specified promises that are completed.
     * @param promises The collection of promises.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the completion of the first of the completed
     * {@code promises}.
     */
    public static <V> Promise<V> whenAnyCompleted(Promise<V>[] promises) {
        if (promises == null || promises.length == 0) {
            Deferred<V> deferred = Promises.defer();
            return deferred.promise();
        }
        return whenAnyCompleted(Arrays.asList(promises));
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed propagating the result or rejection of the first
     * of the specified promises that are completed.
     * @param promises The collection of promises.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the completion of the first of the completed
     * {@code promises}.
     */
    public static <V> Promise<V> whenAnyCompleted(Iterable<? extends Promise<V>> promises) {
        final Deferred<V> deferred = Promises.defer();
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
                            case FULFILLED:
                                deferred.fulfill(result);
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

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed with the result of the first fulfilled promise.  If
     * none of the specified promises fulfill successfully then the returned promise will never be completed.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the fulfilled result of the first of the fulfilled promises.
     */
    public static <V> Promise<V> whenAnyFulfilled(Promise<V> promise1, Promise<V> promise2) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(2);
        promises.add(promise1);
        promises.add(promise2);
        return whenAnyFulfilled(promises);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed with the result of the first fulfilled promise.  If
     * none of the specified promises fulfill successfully then the returned promise will never be completed.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param promise3 The third promise.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the fulfilled result of the first of the fulfilled promises.
     */
    public static <V> Promise<V> whenAnyFulfilled(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(3);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        return whenAnyFulfilled(promises);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed with the result of the first fulfilled promise.  If
     * none of the specified promises fulfill successfully then the returned promise will never be completed.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param promise3 The third promise.
     * @param promise4 The fourth promise.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the fulfilled result of the first of the fulfilled promises.
     */
    public static <V> Promise<V> whenAnyFulfilled(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(4);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);
        return whenAnyFulfilled(promises);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed with the result of the first fulfilled promise.  If
     * none of the specified promises fulfill successfully then the returned promise will never be completed.
     * @param promise1 The first promise.
     * @param promise2 The second promise.
     * @param promise3 The third promise.
     * @param promise4 The fourth promise.
     * @param promise5 The fifth promise.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the fulfilled result of the first of the fulfilled promises.
     */
    public static <V> Promise<V> whenAnyFulfilled(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4, Promise<V> promise5) {
        List<Promise<V>> promises = new ArrayList<Promise<V>>(5);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);
        promises.add(promise5);
        return whenAnyFulfilled(promises);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed with the result of the first fulfilled promise.  If
     * none of the specified promises fulfill successfully then the returned promise will never be completed.
     * @param promises The array of promises.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the fulfilled result of the first of the fulfilled
     * {@code promises}.
     */
    public static <V> Promise<V> whenAnyFulfilled(Promise<V>[] promises) {
        if (promises == null || promises.length == 0) {
            Deferred<V> deferred = Promises.defer();
            return deferred.promise();
        }
        return whenAnyFulfilled(Arrays.asList(promises));
    }

    /**
     * Returns a new {@link org.jpromise.Promise} which is completed with the result of the first fulfilled promise.  If
     * none of the specified promises fulfill successfully then the returned promise will never be completed.
     * @param promises The collection of promises.
     * @param <V> The result type of the promises.
     * @return A {@link org.jpromise.Promise} which will propagate the fulfilled result of the first of the fulfilled
     * {@code promises}.
     */
    public static <V> Promise<V> whenAnyFulfilled(Iterable<? extends Promise<V>> promises) {
        final Deferred<V> deferred = Promises.defer();
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
                    if (promise.isFulfilled() && done.compareAndSet(false, true)) {
                        deferred.fulfill(result);
                    }
                }
            });
        }

        return deferred.promise();
    }
}
