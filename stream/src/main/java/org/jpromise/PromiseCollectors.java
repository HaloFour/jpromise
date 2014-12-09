package org.jpromise;

import org.jpromise.functions.OnResolvedFunction;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PromiseCollectors {
    private static abstract class SingleAccumulatorPromiseCollector<V, A, R> implements PromiseCollector<V, A, R> {
        private final AtomicBoolean done = new AtomicBoolean();
        private final A accumulator;

        protected SingleAccumulatorPromiseCollector(A accumulator) {
            this.accumulator = accumulator;
        }

        @Override
        public final A getAccumulator() {
            if (done.compareAndSet(false, true)) {
                return accumulator;
            }
            throw new IllegalStateException();
        }
    }

    private PromiseCollectors() { }

    public static <V> PromiseCollector<V, ?, V[]> toArray(Class<V> resultClass) {
        return toArray(resultClass, 0);
    }

    public static <V> PromiseCollector<V, ?, V[]> toArray(Class<V> resultClass, final int initialCapacity) {
        final ArrayList<V> list = new ArrayList<>(initialCapacity);
        return new PromiseCollector<V, ArrayList<V>, V[]>() {
            @Override
            public ArrayList<V> getAccumulator() {
                return new ArrayList<>(initialCapacity);
            }

            @Override
            public synchronized void accumulate(ArrayList<V> accumulator, V result) throws Throwable {
                accumulator.add(result);
            }

            @Override
            @SuppressWarnings("unchecked")
            public V[] finish(ArrayList<V> accumulator) {
                return (V[])accumulator.toArray();
            }
        };
    }

    public static <V> PromiseCollector<V, ?, V[]> toArray(final V[] array) {
        final AtomicBoolean done = new AtomicBoolean();
        class ArrayWrapper {
            public V[] array;
            public final AtomicInteger index = new AtomicInteger();
        }
        ArrayWrapper wrapper = new ArrayWrapper();
        wrapper.array = array;
        return new SingleAccumulatorPromiseCollector<V, ArrayWrapper, V[]>(wrapper) {
            @Override
            public void accumulate(ArrayWrapper accumulator, V result) throws Throwable {
                accumulator.array[accumulator.index.getAndIncrement()] = result;
            }

            @Override
            public V[] finish(ArrayWrapper accumulator) {
                return accumulator.array;
            }
        };
    }

    public static <V> PromiseCollector<V, ?, V[]> toArray(Class<V> resultClass, final Callable<V[]> arrayFactory) {
        class ArrayWrapper {
            public V[] array;
            public final AtomicInteger index = new AtomicInteger();
        }
        return new PromiseCollector<V, ArrayWrapper, V[]>() {
            @Override
            public ArrayWrapper getAccumulator() throws Throwable {
                ArrayWrapper wrapper = new ArrayWrapper();
                wrapper.array = arrayFactory.call();
                return wrapper;
            }

            @Override
            public void accumulate(ArrayWrapper accumulator, V result) throws Throwable {
                accumulator.array[accumulator.index.getAndIncrement()] = result;
            }

            @Override
            public V[] finish(ArrayWrapper accumulator) {
                return accumulator.array;
            }
        };
    }

    public static <V> PromiseCollector<V, ?, List<V>> toList(Class<V> resultClass) {
        return toCollection(resultClass, new Callable<List<V>>() {
            @Override
            public List<V> call() throws Exception {
                return new LinkedList<V>();
            }
        });
    }

    public static <V, L extends List<V>> PromiseCollector<V, ?, L> toList(L list) {
        return toCollection(list);
    }

    public static <V, L extends List<V>> PromiseCollector<V, ?, L> toList(Class<V> resultClass, Callable<L> listFactory) {
        return toCollection(resultClass, listFactory);
    }

    public static <V> PromiseCollector<V, ?, Set<V>> toSet(Class<V> resultClass) {
        return toCollection(resultClass, new Callable<Set<V>>() {
            @Override
            public Set<V> call() throws Exception {
                return new HashSet<V>();
            }
        });
    }

    public static <V, S extends Set<V>> PromiseCollector<V, ?, S> toSet(S set) {
        return toCollection(set);
    }

    public static <V> PromiseCollector<V, ?, Collection<V>> toCollection(Class<V> resultClass) {
        return toCollection(resultClass, new Callable<Collection<V>>() {
            @Override
            public Collection<V> call() throws Exception {
                return new LinkedList<>();
            }
        });
    }

    public static <V, C extends Collection<V>> PromiseCollector<V, ?, C> toCollection(Class<V> resultClass, final Callable<C> collectionFactory) {
        return new PromiseCollector<V, C, C>() {
            @Override
            public C getAccumulator() throws Throwable {
                return collectionFactory.call();
            }

            @Override
            public synchronized void accumulate(C accumulator, V result) throws Throwable {
                accumulator.add(result);
            }

            @Override
            public synchronized C finish(C accumulator) {
                return accumulator;
            }
        };
    }

    public static <V, C extends Collection<V>> PromiseCollector<V, ?, C> toCollection(final C collection) {
        return new SingleAccumulatorPromiseCollector<V, C, C>(collection) {
            @Override
            public synchronized void accumulate(C accumulator, V result) throws Throwable {
                accumulator.add(result);
            }

            @Override
            public synchronized C finish(C accumulator) {
                return accumulator;
            }
        };
    }

    public static <V, MK> PromiseCollector<V, ?, Map<MK, V>> toMap(Class<MK> keyClass, Class<V> valueClass, OnResolvedFunction<V, MK> keyMapper) {
        return toMap(new Callable<Map<MK, V>>() {
            @Override
            public Map<MK, V> call() throws Exception {
                return new HashMap<>();
            }
        }, keyMapper, new OnResolvedFunction<V, V>() {
            @Override
            public V resolved(V result) throws Throwable {
                return result;
            }
        });
    }

    public static <V, MK, MV> PromiseCollector<V, ?, Map<MK, MV>> toMap(Class<MK> keyClass, Class<MV> valueClass, OnResolvedFunction<V, MK> keyMapper, OnResolvedFunction<V, MV> valueMapper) {
        return toMap(new Callable<Map<MK, MV>>() {
            @Override
            public Map<MK, MV> call() throws Exception {
                return new HashMap<>();
            }
        }, keyMapper, valueMapper);
    }

    public static <V, MK, M extends Map<MK, V>> PromiseCollector<V, ?, M> toMap(M map, OnResolvedFunction<V, MK> keyMapper) {
        return toMap(map, keyMapper, new OnResolvedFunction<V, V>() {
            @Override
            public V resolved(V result) throws Throwable {
                return result;
            }
        });
    }

    public static <V, MK, MV, M extends Map<MK, MV>> PromiseCollector<V, ?, M> toMap(final M map, final OnResolvedFunction<V, MK> keyMapper, final OnResolvedFunction<V, MV> valueMapper) {
        final Object lock = new Object();
        return new SingleAccumulatorPromiseCollector<V, M, M>(map) {
            @Override
            public void accumulate(M accumulator, V result) throws Throwable {
                MK key = keyMapper.resolved(result);
                MV value = valueMapper.resolved(result);
                synchronized (lock) {
                    accumulator.put(key, value);
                }
            }

            @Override
            public M finish(M accumulator) {
                return accumulator;
            }
        };
    }

    public static <V, MK, MV, M extends Map<MK, MV>> PromiseCollector<V, ?, M> toMap(final Callable<M> mapFactory, final OnResolvedFunction<V, MK> keyMapper, final OnResolvedFunction<V, MV> valueMapper) {
        final Object lock = new Object();
        return new PromiseCollector<V, M, M>() {
            @Override
            public M getAccumulator() throws Throwable {
                return mapFactory.call();
            }

            @Override
            public void accumulate(M accumulator, V result) throws Throwable {
                MK key = keyMapper.resolved(result);
                MV value = valueMapper.resolved(result);
                synchronized (lock) {
                    accumulator.put(key, value);
                }
            }

            @Override
            public M finish(M accumulator) {
                return accumulator;
            }
        };
    }

    public static <V, A, K, C> PromiseCollector<V, ?, Map<K, C>> groupingBy(Class<K> keyClass, final OnResolvedFunction<V, K> keyMapper, final PromiseCollector<V, A, C> groupCollector) {
        Map<K, C> groupMap = new HashMap<>();
        return groupingBy(groupMap, keyMapper, groupCollector);
    }

    public static <V, A, K, C, M extends Map<K, C>> PromiseCollector<V, ?, M> groupingBy(final M map, final OnResolvedFunction<V, K> keyMapper, final PromiseCollector<V, A, C> groupCollector) {
        final Object lock = new Object();
        return new PromiseCollector<V, Map<K, A>, M>() {
            @Override
            public Map<K, A> getAccumulator() throws Throwable {
                return new HashMap<K, A>();
            }

            @Override
            public void accumulate(Map<K, A> accumulator, V result) throws Throwable {
                K key = keyMapper.resolved(result);
                A group;
                synchronized (lock) {
                    if (accumulator.containsKey(key)) {
                        group = accumulator.get(key);
                    }
                    else {
                        group = groupCollector.getAccumulator();
                        accumulator.put(key, group);
                    }
                }
                groupCollector.accumulate(group, result);
            }

            @Override
            public M finish(Map<K, A> accumulator) {
                for (Map.Entry<K, A> entry : accumulator.entrySet()) {
                    C group = groupCollector.finish(entry.getValue());
                    map.put(entry.getKey(), group);
                }
                return map;
            }
        };
    }
}
