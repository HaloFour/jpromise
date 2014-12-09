package org.jpromise;

import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnRejectedHandler;
import org.jpromise.functions.OnResolvedFunction;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class PromiseStream<V> {
    private final OnSubscribe<V> subscribe;

    private PromiseStream(Promise<V>[] promises) {
        this(promises != null ? Arrays.asList(promises) : null);
    }

    private PromiseStream(final Iterable<Promise<V>> promises) {
        this(new PromiseSource<>(promises));
    }

    private PromiseStream(OnSubscribe<V> subscribe) {
        this.subscribe = subscribe;
    }

    <V_APPLIED> PromiseStream<V_APPLIED> map(final OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return new PromiseStream<>(new StreamOperator<V, V_APPLIED>(subscribe) {
            @Override
            protected void resolved(PromiseSubscriber<V_APPLIED> subscriber, V result) throws Throwable {
                subscriber.resolved(function.resolved(result));
            }
        });
    }

    <V_COMPOSED> PromiseStream<V_COMPOSED> compose(final OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        final AtomicInteger counter = new AtomicInteger();
        counter.set(1);
        return new PromiseStream<>(new StreamOperator<V, V_COMPOSED>(subscribe) {
            @Override
            protected void resolved(final PromiseSubscriber<V_COMPOSED> subscriber, V result) throws Throwable {
                Future<V_COMPOSED> future = function.resolved(result);
                if (future == null) {
                    subscriber.resolved(null);
                    return;
                }
                counter.incrementAndGet();
                Promise<V_COMPOSED> promise = PromiseManager.fromFuture(future);
                promise.whenCompleted(new OnCompleted<V_COMPOSED>() {
                    @Override
                    public void completed(Promise<V_COMPOSED> promise, V_COMPOSED result, Throwable exception) throws Throwable {
                        switch (promise.state()) {
                            case RESOLVED:
                                subscriber.resolved(result);
                                break;
                            case REJECTED:
                                subscriber.rejected(exception);
                                break;
                        }
                        if (counter.decrementAndGet() == 0) {
                            subscriber.complete();
                        }
                    }
                });
            }

            @Override
            protected void complete(PromiseSubscriber<V_COMPOSED> subscriber) {
                if (counter.decrementAndGet() == 0) {
                    subscriber.complete();
                }
            }
        });
    }

    public PromiseStream<V> filter(final OnResolvedFunction<V, Boolean> predicate) {
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        return new PromiseStream<>(new StreamOperator<V, V>(subscribe) {
            @Override
            protected void resolved(PromiseSubscriber<V> subscriber, V result) throws Throwable {
                Boolean filter = predicate.resolved(result);
                if (filter != null && filter) {
                    subscriber.resolved(result);
                }
            }
        });
    }

    public PromiseStream<V> filterNulls() {
        return filter(new OnResolvedFunction<V, Boolean>() {
            @Override
            public Boolean resolved(V result) throws Throwable {
                return (result != null);
            }
        });
    }

    public PromiseStream<V> filterRejected() {
        return filterRejected(Throwable.class, new OnRejectedHandler<Throwable, Boolean>() {
            @Override
            public Boolean handle(Throwable exception) {
                return true;
            }
        });
    }

    public PromiseStream<V> filterRejected(OnRejectedHandler<Throwable, Boolean> predicate) {
        return filterRejected(Throwable.class, predicate);
    }

    public <E extends Throwable> PromiseStream<V> filterRejected(final Class<E> exceptionClass, final OnRejectedHandler<? super E, Boolean> predicate) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        return new PromiseStream<V>(new StreamOperator<V, V>(subscribe) {
            @Override
            protected void resolved(PromiseSubscriber<V> subscriber, V result) {
                subscriber.resolved(result);
            }

            @Override
            protected void rejected(PromiseSubscriber<V> subscriber, Throwable exception) throws Throwable {
                if (exceptionClass.isInstance(exception)) {
                    E typed = exceptionClass.cast(exception);
                    Boolean filter = predicate.handle(typed);
                    if (filter == null || !filter) {
                        subscriber.rejected(exception);
                    }
                }
                else {
                    subscriber.rejected(exception);
                }
            }
        });
    }

    public Promise<? extends List<V>> toList(Class<V> resultClass) {
        return collect(PromiseCollectors.toList(resultClass));
    }

    public Promise<List<V>> toList(List<V> list) {
        return collect(PromiseCollectors.toList(list));
    }

    public Promise<Set<V>> toSet(Set<V> set) {
        return collect(PromiseCollectors.toSet(set));
    }

    public Promise<V[]> toArray(V[] array) {
        return collect(PromiseCollectors.toArray(array));
    }

    public <C extends Collection<V>> Promise<C> toCollection(C collection) {
        return collect(PromiseCollectors.toCollection(collection));
    }

    public <K> Promise<Map<K, V>> toMap(Map<K, V> map, OnResolvedFunction<V, K> keyMapper) {
        return collect(PromiseCollectors.toMap(map, keyMapper));
    }

    public <K, MV> Promise<Map<K, MV>> toMap(Map<K, MV> map, OnResolvedFunction<V, K> keyMapper, OnResolvedFunction<V, MV> valueMapper) {
        return collect(PromiseCollectors.toMap(map, keyMapper, valueMapper));
    }

    public <A, R> Promise<R> collect(PromiseCollector<V, A, R> collector) {
        if (collector == null) throw new IllegalArgumentException(mustNotBeNull("collector"));
        try {
            A accumulator = collector.getAccumulator();
            return collect(accumulator, collector);
        }
        catch (Throwable exception) {
            return Promise.rejected(exception);
        }
    }

    private <A, R> Promise<R> collect(final A accumulator, final PromiseCollector<V, A, R> collector) {
        final Deferred<R> deferred = Promise.defer();
        final AtomicBoolean done = new AtomicBoolean();
        subscribe.subscribed(new PromiseSubscriber<V>() {
            @Override
            public void resolved(V result) {
                try {
                    if (!done.get()) {
                        collector.accumulate(accumulator, result);
                    }
                }
                catch (Throwable exception) {
                    rejected(exception);
                }
            }

            @Override
            public void rejected(Throwable exception) {
                if (done.compareAndSet(false, true)) {
                    deferred.reject(exception);
                }
            }

            @Override
            public void complete() {
                if (done.compareAndSet(false, true)) {
                    try {
                        R result = collector.finish(accumulator);
                        deferred.resolve(result);
                    }
                    catch (Throwable exception) {
                        deferred.reject(exception);
                    }
                }
            }
        });
        return deferred.promise();
    }

    @SafeVarargs
    public static <V> PromiseStream<V> from(Promise<V>... promises) {
        return new PromiseStream<>(promises);
    }

    public static <V> PromiseStream<V> from(Iterable<Promise<V>> promises) {
        return new PromiseStream<>(promises);
    }
}
