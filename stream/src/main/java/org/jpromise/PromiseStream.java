package org.jpromise;

import org.jpromise.functions.*;
import org.jpromise.operators.*;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface PromiseStream<V> {
    Promise<Void> subscribe(PromiseSubscriber<? super V> subscriber);
    <V_APPLIED> PromiseStream<V_APPLIED> map(final OnFulfilledFunction<? super V, ? extends V_APPLIED> function);
    <V_APPLIED> PromiseStream<V_APPLIED> flatMap(final OnFulfilledFunction<? super V, ? extends Iterable<? extends V_APPLIED>> function);
    <V_COMPOSED> PromiseStream<V_COMPOSED> compose(final OnFulfilledFunction<? super V, ? extends Future<V_COMPOSED>> function);
    PromiseStream<V> filter(final OnFulfilledFunction<V, Boolean> predicate);
    PromiseStream<V> filterNulls();
    PromiseStream<V> filterRejected();
    PromiseStream<V> filterRejected(OnRejectedHandler<Throwable, Boolean> predicate);
    <E extends Throwable> PromiseStream<V> filterRejected(Class<E> exceptionClass);
    <E extends Throwable> PromiseStream<V> filterRejected(final Class<E> exceptionClass, final OnRejectedHandler<? super E, Boolean> predicate);
    PromiseStream<V> take(int count);
    PromiseStream<V> takeUntil(long timeout, TimeUnit timeUnit);
    <V2> PromiseStream<V> takeUntil(Promise<V2> promise);
    PromiseStream<V> lift(StreamOperator<V, V> operator);
    <V_OUT> PromiseStream<V_OUT> translate(StreamOperator<V, V_OUT> operator);
    Promise<Void> forEach(OnFulfilled<V> action);
    Promise<List<V>> toList(Class<V> resultClass);
    Promise<List<V>> toList(List<V> list);
    Promise<Set<V>> toSet(Class<V> resultClass);
    Promise<Set<V>> toSet(Set<V> set);
    Promise<V[]> toArray(Class<V> resultClass);
    Promise<V[]> toArray(Class<V> resultClass, int initialCapacity);
    Promise<V[]> toArray(V[] array);
    Promise<Collection<V>> toCollection(Class<V> resultClass);
    <C extends Collection<V>> Promise<C> toCollection(C collection);
    <K> Promise<Map<K, V>> toMap(Class<K> keyClass, Class<V> valueClass, OnFulfilledFunction<V, K> keyMapper);
    <K, MV> Promise<Map<K, MV>> toMap(Class<K> keyClass, Class<MV> valueClass, OnFulfilledFunction<V, K> keyMapper, OnFulfilledFunction<V, MV> valueMapper);
    <K> Promise<Map<K, V>> toMap(Map<K, V> map, OnFulfilledFunction<V, K> keyMapper);
    <K, MV> Promise<Map<K, MV>> toMap(Map<K, MV> map, OnFulfilledFunction<V, K> keyMapper, OnFulfilledFunction<V, MV> valueMapper);
    <A, R> Promise<R> collect(PromiseCollector<V, A, R> collector);
    <R> Promise<R> terminate(TerminalOperator<V, R> operator);
    Iterable<V> toIterable();
}
