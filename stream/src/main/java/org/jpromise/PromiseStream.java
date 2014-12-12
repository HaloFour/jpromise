package org.jpromise;

import org.jpromise.functions.OnRejectedHandler;
import org.jpromise.functions.OnResolvedFunction;
import org.jpromise.operators.*;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public <V_APPLIED> PromiseStream<V_APPLIED> map(final OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return new PromiseStream<>(new MapOperator<>(subscribe, function));
    }

    public <V_APPLIED> PromiseStream<V_APPLIED> flatMap(final OnResolvedFunction<? super V, ? extends Iterable<? extends V_APPLIED>> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return new PromiseStream<>(new FlatMapOperator<>(subscribe, function));
    }

    public <V_COMPOSED> PromiseStream<V_COMPOSED> compose(final OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return new PromiseStream<>(new ComposeOperator<>(subscribe, function));
    }

    public PromiseStream<V> filter(final OnResolvedFunction<V, Boolean> predicate) {
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        return new PromiseStream<>(new FilterOperator<>(subscribe, predicate));
    }

    public PromiseStream<V> filterNulls() {
        return new PromiseStream<>(new FilterNullOperator<>(subscribe));
    }

    public PromiseStream<V> filterRejected() {
        return new PromiseStream<V>(new FilterRejectedOperator<>(subscribe, Throwable.class));
    }

    public PromiseStream<V> filterRejected(OnRejectedHandler<Throwable, Boolean> predicate) {
        return filterRejected(Throwable.class, predicate);
    }

    public <E extends Throwable> PromiseStream<V> filterRejected(final Class<E> exceptionClass, final OnRejectedHandler<? super E, Boolean> predicate) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        return new PromiseStream<V>(new FilterRejectedOperator<>(subscribe, exceptionClass, predicate));
    }

    public PromiseStream<V> take(int count) {
        return new PromiseStream<>(new TakeOperator<>(subscribe, count));
    }

    public Promise<List<V>> toList(Class<V> resultClass) {
        return collect(PromiseCollectors.toList(resultClass));
    }

    public Promise<List<V>> toList(List<V> list) {
        return collect(PromiseCollectors.toList(list));
    }

    public Promise<Set<V>> toSet(Class<V> resultClass) {
        return collect(PromiseCollectors.toSet(resultClass));
    }

    public Promise<Set<V>> toSet(Set<V> set) {
        return collect(PromiseCollectors.toSet(set));
    }

    public Promise<V[]> toArray(Class<V> resultClass) {
        return collect(PromiseCollectors.toArray(resultClass));
    }

    public Promise<V[]> toArray(Class<V> resultClass, int initialCapacity) {
        return collect(PromiseCollectors.toArray(resultClass, initialCapacity));
    }

    public Promise<V[]> toArray(V[] array) {
        return collect(PromiseCollectors.toArray(array));
    }

    public Promise<Collection<V>> toCollection(Class<V> resultClass) {
        return collect(PromiseCollectors.toCollection(resultClass));
    }

    public <C extends Collection<V>> Promise<C> toCollection(C collection) {
        return collect(PromiseCollectors.toCollection(collection));
    }

    public <K> Promise<Map<K, V>> toMap(Class<K> keyClass, Class<V> valueClass, OnResolvedFunction<V, K> keyMapper) {
        return collect(PromiseCollectors.toMap(keyClass, valueClass, keyMapper));
    }

    public <K, MV> Promise<Map<K, MV>> toMap(Class<K> keyClass, Class<MV> valueClass, OnResolvedFunction<V, K> keyMapper, OnResolvedFunction<V, MV> valueMapper) {
        return collect(PromiseCollectors.toMap(keyClass, valueClass, keyMapper, valueMapper));
    }

    public <K> Promise<Map<K, V>> toMap(Map<K, V> map, OnResolvedFunction<V, K> keyMapper) {
        return collect(PromiseCollectors.toMap(map, keyMapper));
    }

    public <K, MV> Promise<Map<K, MV>> toMap(Map<K, MV> map, OnResolvedFunction<V, K> keyMapper, OnResolvedFunction<V, MV> valueMapper) {
        return collect(PromiseCollectors.toMap(map, keyMapper, valueMapper));
    }

    public <A, R> Promise<R> collect(PromiseCollector<V, A, R> collector) {
        if (collector == null) throw new IllegalArgumentException(mustNotBeNull("collector"));
        CollectOperator<V, A, R> operator = new CollectOperator<>(subscribe, collector);
        return operator.subscribe();
    }

    @SafeVarargs
    public static <V> PromiseStream<V> from(Promise<V>... promises) {
        return new PromiseStream<>(promises);
    }

    public static <V> PromiseStream<V> from(Iterable<Promise<V>> promises) {
        return new PromiseStream<>(promises);
    }
}
