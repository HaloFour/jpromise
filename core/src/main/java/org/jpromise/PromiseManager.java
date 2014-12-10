package org.jpromise;

import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnResolved;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class PromiseManager {
    private PromiseManager() { }

    public static <Void> Promise<Void> create(Runnable runnable) {
        return create(PromiseExecutors.DEFAULT_CREATION_EXECUTOR, runnable);
    }

    public static <V> Promise<V> create(Runnable runnable, V result) {
        return create(PromiseExecutors.DEFAULT_CREATION_EXECUTOR, runnable, result);
    }

    public static <V> Promise<V> create(Callable<V> callable) {
        return create(PromiseExecutors.DEFAULT_CREATION_EXECUTOR, callable);
    }

    public static <Void> Promise<Void> create(Executor executor, Runnable runnable) {
        return create(executor, runnable, null);
    }

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

    public static <V> Promise<V> fromFuture(Future<V> future) {
        return fromFuture(PromiseExecutors.DEFAULT_FUTURE_EXECUTOR, future);
    }

    public static <V> Promise<V> fromFuture(Executor executor, Future<V> future) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (future == null) throw new IllegalArgumentException(mustNotBeNull("future"));
        if (future instanceof Promise) {
            return (Promise<V>)future;
        }
        return new FuturePromise<>(executor, future);
    }

    public static <V> Promise<V> fromFuture(Future<V> future, long timeout, TimeUnit timeUnit) {
        return fromFuture(PromiseExecutors.DEFAULT_FUTURE_EXECUTOR, future, timeout, timeUnit);
    }

    public static <V> Promise<V> fromFuture(Executor executor, Future<V> future, long timeout, TimeUnit timeUnit) {
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        if (future == null) throw new IllegalArgumentException(mustNotBeNull("future"));
        if (timeUnit == null) throw new IllegalArgumentException(mustNotBeNull("timeUnit"));
        if (future instanceof Promise) {
            return (Promise<V>)future;
        }
        return new FuturePromise<>(executor, future, timeout, timeUnit);
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

    public static Promise<Void> whenAllCompleted(Promise<?>... promises) {
        if (promises == null || promises.length == 0) {
            return Promise.resolved(null);
        }
        return whenAllCompleted(Arrays.asList(promises));
    }

    @SuppressWarnings("unchecked")
    public static Promise<Void> whenAllCompleted(Iterable<? extends Promise<?>> promises) {
        return whenAllCompletedImpl((Iterable)promises, null, null);
    }

    public static <V> Promise<Void> whenAllCompleted(Iterable<? extends Promise<V>> promises, OnCompleted<V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        return whenAllCompletedImpl(promises, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }
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

    public static Promise<Void> whenAllResolved(Promise<?>... promises) {
        if (promises == null || promises.length == 0) {
            return Promise.resolved(null);
        }
        return whenAllResolved(Arrays.asList(promises));
    }

    @SuppressWarnings("unchecked")
    public static Promise<Void> whenAllResolved(Iterable<? extends Promise<?>> promises) {
        return whenAllResolvedImpl((Iterable)promises, null, null);
    }

    public static <V> Promise<Void> whenAllResolved(Iterable<? extends Promise<V>> promises, OnResolved<? super V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        return whenAllResolvedImpl(promises, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    public static <V> Promise<Void> whenAllResolved(Iterable<? extends Promise<V>> promises, Executor executor, OnResolved<? super V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        if (executor == null) throw new IllegalArgumentException(mustNotBeNull("executor"));
        return whenAllResolvedImpl(promises, executor, action);
    }

    public static <V> Promise<Void> whenAllResolvedImpl(Iterable<? extends Promise<V>> promises, Executor executor, OnResolved<? super V> action) {
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

    @SafeVarargs
    public static <V> Promise<V> whenAnyCompleted(Promise<V>... promises) {
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

    @SafeVarargs
    public static <V> Promise<V> whenAnyResolved(Promise<V>... promises) {
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
