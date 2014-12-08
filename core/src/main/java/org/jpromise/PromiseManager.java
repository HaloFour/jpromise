package org.jpromise;

import org.jpromise.functions.OnCompleted;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class PromiseManager {
    private static final Executor FUTURE_EXECUTOR = PromiseExecutors.NEW;

    private PromiseManager() { }

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
        return fromFuture(PromiseExecutors.NEW, future);
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
        return fromFuture(FUTURE_EXECUTOR, future, timeout, timeUnit);
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

    private static <V> void whenCompleted(Promise<V> promise, final Deferred<Void> deferred, final AtomicInteger counter) {
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

    public static Promise<Void> whenAllCompleted(Iterable<? extends Promise<?>> promises) {
        if (promises == null) {
            return Promise.resolved(null);
        }

        final Deferred<Void> deferred = Promise.defer();
        final AtomicInteger counter = new AtomicInteger();

        counter.incrementAndGet();
        for (final Promise<?> promise : promises) {
            if (promise == null) {
                continue;
            }
            counter.incrementAndGet();
            whenCompleted(promise, deferred, counter);
        }
        if (counter.decrementAndGet() <= 0) {
            deferred.resolve(null);
        }

        return deferred.promise();
    }

    private static <V> void whenResolved(Promise<V> promise, final Deferred<Void> deferred, final AtomicInteger counter, final AtomicBoolean done) {
        promise.whenCompleted(new OnCompleted<V>() {
            @Override
            public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                if (promise.isRejected()) {
                    if (done.compareAndSet(false, true)) {
                        deferred.reject(exception);
                    }
                } else {
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

    public static Promise<Void> whenAllResolved(Iterable<? extends Promise<?>> promises) {
        if (promises == null) {
            return Promise.resolved(null);
        }

        final Deferred<Void> deferred = Promise.defer();
        final AtomicInteger counter = new AtomicInteger();
        final AtomicBoolean done = new AtomicBoolean();

        counter.incrementAndGet();
        for (final Promise<?> promise : promises) {
            if (promise == null) {
                continue;
            }
            counter.incrementAndGet();
            whenResolved(promise, deferred, counter, done);
        }
        if (counter.decrementAndGet() <= 0 && done.compareAndSet(false, true)) {
            deferred.resolve(null);
        }

        return deferred.promise();
    }

    @SafeVarargs
    public static <V> Promise<V> whenAnyComplete(Promise<V>... promises) {
        if (promises == null || promises.length == 0) {
            Deferred<V> deferred = Promise.defer();
            return deferred.promise();
        }
        return whenAnyComplete(Arrays.asList(promises));
    }

    public static <V> Promise<V> whenAnyComplete(Iterable<? extends Promise<V>> promises) {
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
