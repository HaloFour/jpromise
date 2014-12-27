package org.jpromise;

import org.jpromise.functions.OnRejectedHandler;
import org.jpromise.functions.OnResolved;
import org.jpromise.functions.OnResolvedFunction;
import org.jpromise.operators.*;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public abstract class PromiseStream<V> {
    public abstract Promise<Void> subscribe(PromiseSubscriber<? super V> subscriber);

    public <V_APPLIED> PromiseStream<V_APPLIED> map(final OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return translate(new MapOperator<V, V_APPLIED>(function));
    }

    public <V_APPLIED> PromiseStream<V_APPLIED> flatMap(final OnResolvedFunction<? super V, ? extends Iterable<? extends V_APPLIED>> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return translate(new FlatMapOperator<V, V_APPLIED>(function));
    }

    public <V_COMPOSED> PromiseStream<V_COMPOSED> compose(final OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        return translate(new ComposeOperator<V, V_COMPOSED>(function));
    }

    public PromiseStream<V> filter(final OnResolvedFunction<V, Boolean> predicate) {
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        return lift(new FilterOperator<V>(predicate));
    }

    public PromiseStream<V> filterNulls() {
        return lift(new FilterNullOperator<V>());
    }

    public PromiseStream<V> filterRejected() {
        return lift(new FilterRejectedOperator<V, Throwable>(Throwable.class));
    }

    public PromiseStream<V> filterRejected(OnRejectedHandler<Throwable, Boolean> predicate) {
        return filterRejected(Throwable.class, predicate);
    }

    public <E extends Throwable> PromiseStream<V> filterRejected(Class<E> exceptionClass) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        return lift(new FilterRejectedOperator<V, E>(exceptionClass));
    }

    public <E extends Throwable> PromiseStream<V> filterRejected(final Class<E> exceptionClass, final OnRejectedHandler<? super E, Boolean> predicate) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        return lift(new FilterRejectedOperator<V, E>(exceptionClass, predicate));
    }

    public PromiseStream<V> take(int count) {
        return lift(new TakeOperator<V>(count));
    }

    public PromiseStream<V> takeUntil(long timeout, TimeUnit timeUnit) {
        return takeUntil(Promises.delay(timeout, timeUnit));
    }

    public <V2> PromiseStream<V> takeUntil(Promise<V2> promise) {
        if (promise == null) throw new IllegalArgumentException(mustNotBeNull("promise"));
        return lift(new TakeUntilOperator<V, V2>(promise));
    }

    public PromiseStream<V> lift(StreamOperator<V, V> operator) {
        if (operator == null) throw new IllegalArgumentException(mustNotBeNull("operator"));
        return new ComposedPromiseStream<V, V>(this, operator);
    }

    public <V_OUT> PromiseStream<V_OUT> translate(StreamOperator<V, V_OUT> operator) {
        if (operator == null) throw new IllegalArgumentException(mustNotBeNull("operator"));
        return new ComposedPromiseStream<V, V_OUT>(this, operator);
    }

    public Promise<Void> forEach(OnResolved<V> action) {
        return terminate(new ForEachOperator<V>(action));
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
        CollectOperator<V, A, R> operator = new CollectOperator<V, A, R>(collector);
        return terminate(operator);
    }

    public <R> Promise<R> terminate(TerminalOperator<V, R> operator) {
        if (operator == null) throw new IllegalArgumentException(mustNotBeNull("operator"));
        return operator.subscribe(this);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(1);
        list.add(promise1);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1, Promise<V> promise2) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(2);
        list.add(promise1);
        list.add(promise2);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(3);
        list.add(promise1);
        list.add(promise2);
        list.add(promise3);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(4);
        list.add(promise1);
        list.add(promise2);
        list.add(promise3);
        list.add(promise4);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4, Promise<V> promise5) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(5);
        list.add(promise1);
        list.add(promise2);
        list.add(promise3);
        list.add(promise4);
        list.add(promise5);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V>[] promises) {
        return new PromiseSource<V>(promises);
    }

    public static <V> PromiseStream<V> from(Iterable<Promise<V>> promises) {
        return new PromiseSource<V>(promises);
    }
}
