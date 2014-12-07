package org.jpromise;

import org.jpromise.functions.OnCompleted;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PromiseManager {
    public static <V> Promise<V> create(final Callable<V> callable, Executor executor) {
        Arg.ensureNotNull(callable, "callable");
        Arg.ensureNotNull(executor, "executor");
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
        Arg.ensureNotNull(future, "future");
        if (future instanceof Promise) {
            return (Promise<V>)future;
        }
        return new FuturePromise<>(future);
    }

    public static <V> Promise<V> fromFuture(Future<V> future, long timeout, TimeUnit timeUnit) {
        Arg.ensureNotNull(future, "future");
        Arg.ensureNotNull(timeUnit, "timeUnit");
        if (future instanceof Promise) {
            return (Promise<V>)future;
        }
        return new FuturePromise<>(future, timeout, timeUnit);
    }

    private static <V> void whenComplete(Promise<V> promise, final Deferred<Void> deferred, final AtomicInteger counter) {
        promise.whenCompleted(new OnCompleted<V>() {
            @Override
            public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                if (counter.decrementAndGet() <= 0) {
                    deferred.resolve(null);
                }
            }
        });
    }

    public static Promise<Void> whenAllComplete(Promise<?>... promises) {
        if (promises == null || promises.length == 0) {
            return Promise.resolved(null);
        }
        return whenAllComplete(Arrays.asList(promises));
    }

    public static Promise<Void> whenAllComplete(Iterable<Promise<?>> promises) {
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
            whenComplete(promise, deferred, counter);
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

    public static Promise<Void> whenAllResolved(Iterable<Promise<?>> promises) {
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

    public static <V> Promise<V> whenAnyComplete(Iterable<Promise<V>> promises) {
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

    public static <V> Promise<V> whenAnyResolved(Iterable<Promise<V>> promises) {
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
