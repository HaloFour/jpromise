package org.jpromise;

import junit.framework.AssertionFailedError;
import org.jpromise.functions.OnRejectedHandler;
import org.jpromise.functions.OnResolved;
import org.jpromise.functions.OnResolvedFunction;
import org.jpromise.operators.TerminalOperation;
import org.jpromise.operators.TerminalOperator;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class PromiseStreamTest {
    public static final String SUCCESS1 = "SUCCESS1";
    public static final String SUCCESS2 = "SUCCESS2";
    public static final String SUCCESS3 = "SUCCESS3";
    public static final String SUCCESS4 = "SUCCESS4";
    public static final String SUCCESS5 = "SUCCESS5";
    public static final String FAIL1 = "FAIL1";
    public static final Throwable EXCEPTION = new ArithmeticException(FAIL1);

    private PromiseStream<String> createStream(boolean includeRejection) {
        List<Promise<String>> promises = new ArrayList<Promise<String>>(6);
        promises.add(resolveAfter(SUCCESS1, 10));
        promises.add(resolveAfter(SUCCESS2, 10));
        promises.add(resolveAfter(SUCCESS3, 10));
        promises.add(resolveAfter(SUCCESS4, 10));
        promises.add(resolveAfter(SUCCESS5, 10));
        if (includeRejection) {
            Promise<String> rejected = rejectAfter(EXCEPTION, 10);
            promises.add(rejected);
        }
        return PromiseStream.from(promises);
    }

    private String reverse(String input) {
        return new StringBuilder(input).reverse().toString();
    }

    private void assertContains(String expected, String[] array) {
        if (array == null) {
            throw new AssertionFailedError(String.format("String \"%s\" not contained within the array because it is null.", expected));
        }
        for (String value : array) {
            if (expected == null && value == null) {
                return;
            }
            if (expected != null && expected.equals(value)) {
                return;
            }
        }
        throw new AssertionFailedError(String.format("String \"s\" not contained within the array."));
    }

    private void assertContainsAll(String[] expected, String[] array) {
        for (String value : expected) {
            assertContains(value, array);
        }
    }

    @Test
    public void from1() throws Throwable {
        PromiseStream<String> stream = PromiseStream.from(Promise.resolved(SUCCESS1));
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertResolves(promise);
        assertEquals(1, result.size());
        assertTrue(result.contains(SUCCESS1));
    }

    @Test
    public void from2() throws Throwable {
        PromiseStream<String> stream = PromiseStream.from(
                Promise.resolved(SUCCESS1),
                Promise.resolved(SUCCESS2)
        );
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertResolves(promise);
        assertEquals(2, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
    }

    @Test
    public void from3() throws Throwable {
        PromiseStream<String> stream = PromiseStream.from(
                Promise.resolved(SUCCESS1),
                Promise.resolved(SUCCESS2),
                Promise.resolved(SUCCESS3)
        );
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertResolves(promise);
        assertEquals(3, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
    }

    @Test
    public void from4() throws Throwable {
        PromiseStream<String> stream = PromiseStream.from(
                Promise.resolved(SUCCESS1),
                Promise.resolved(SUCCESS2),
                Promise.resolved(SUCCESS3),
                Promise.resolved(SUCCESS4)
        );
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertResolves(promise);
        assertEquals(4, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
    }

    @Test
    public void from5() throws Throwable {
        PromiseStream<String> stream = PromiseStream.from(
                Promise.resolved(SUCCESS1),
                Promise.resolved(SUCCESS2),
                Promise.resolved(SUCCESS3),
                Promise.resolved(SUCCESS4),
                Promise.resolved(SUCCESS5)
        );
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    private Promise<String>[] makeArray(Promise<String>... promises) {
        return promises;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fromArray() throws Throwable {
        Promise<String>[] array = makeArray(Promise.resolved(SUCCESS1),
                Promise.resolved(SUCCESS2),
                Promise.resolved(SUCCESS3),
                Promise.resolved(SUCCESS4),
                Promise.resolved(SUCCESS5)
        );

        PromiseStream<String> stream = PromiseStream.from(array);
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void fromNull() throws Throwable {
        Iterable<Promise<String>> iterable = null;
        PromiseStream<String> stream = PromiseStream.from(iterable);
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertResolves(promise);
        assertEquals(0, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void forEach() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        OnResolved<String> callback = mock(OnResolved.class);
        Promise<Void> promise = stream.forEach(callback);

        assertResolves(promise);
        verify(callback, times(1)).resolved(eq(SUCCESS1));
        verify(callback, times(1)).resolved(eq(SUCCESS2));
        verify(callback, times(1)).resolved(eq(SUCCESS3));
        verify(callback, times(1)).resolved(eq(SUCCESS4));
        verify(callback, times(1)).resolved(eq(SUCCESS5));
    }

    @Test
    public void toArray() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.toArray(String.class);

        String[] result = assertResolves(promise);

        assertContainsAll(new String[]{
                        SUCCESS1,
                        SUCCESS2,
                        SUCCESS3,
                        SUCCESS4,
                        SUCCESS5},
                result);
    }

    @Test
    public void toArrayWithInitialCapacity() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.toArray(String.class, 5);

        String[] result = assertResolves(promise);

        assertContainsAll(new String[]{
                        SUCCESS1,
                        SUCCESS2,
                        SUCCESS3,
                        SUCCESS4,
                        SUCCESS5},
                result);
    }

    @Test
    public void toArrayWithFactory() throws Throwable {
        PromiseStream<String> stream = createStream(false);

        Callable<String[]> factory = new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                return new String[5];
            }
        };

        Promise<String[]> promise = stream.collect(PromiseCollectors.toArray(String.class, factory));

        String[] result = assertResolves(promise);

        assertContainsAll(new String[]{
                        SUCCESS1,
                        SUCCESS2,
                        SUCCESS3,
                        SUCCESS4,
                        SUCCESS5},
                result);
    }

    @Test
    public void toArrayOutOfBounds() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.toArray(new String[4]);
        assertRejects(IndexOutOfBoundsException.class, promise);
    }

    @Test
    public void collectToExistingArray() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.collect(PromiseCollectors.toArray(new String[5]));
        String[] result = assertResolves(promise);

        assertContainsAll(new String[]{
                        SUCCESS1,
                        SUCCESS2,
                        SUCCESS3,
                        SUCCESS4,
                        SUCCESS5},
                result);
    }

    @Test
    public void toList() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<List<String>> promise = stream.toList(String.class);

        List<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void toExistingList() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        List<String> list = new ArrayList<String>();
        Promise<List<String>> promise = stream.toList(list);

        List<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void toListWithFactory() throws Throwable {
        PromiseStream<String> stream = createStream(false);

        Callable<List<String>> factory =new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return new ArrayList<String>();
            }
        };

        Promise<List<String>> promise = stream.collect(PromiseCollectors.toList(String.class, factory));

        List<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void toSet() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<Set<String>> promise = stream.toSet(String.class);

        Set<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void toExistingSet() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Set<String> set = new HashSet<String>();
        Promise<Set<String>> promise = stream.toSet(set);

        Set<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void toSetFromFactory() throws Throwable {
        PromiseStream<String> stream = createStream(false);

        Callable<Set<String>> factory = new Callable<Set<String>>() {
            @Override
            public Set<String> call() throws Exception {
                return new HashSet<String>();
            }
        };

        Promise<Set<String>> promise = stream.collect(PromiseCollectors.toSet(String.class, factory));

        Set<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }


    @Test
    public void toCollection() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<Collection<String>> promise = stream.toCollection(String.class);

        Collection<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void toExistingCollection() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Collection<String> collection = new ArrayList<String>();
        Promise<Collection<String>> promise = stream.toCollection(collection);

        Collection<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void toMap() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<Map<String, String>> promise = stream.toMap(String.class, String.class, new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return result;
            }
        });

        Map<String, String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.containsKey(SUCCESS1));
        assertTrue(result.containsKey(SUCCESS2));
        assertTrue(result.containsKey(SUCCESS3));
        assertTrue(result.containsKey(SUCCESS4));
        assertTrue(result.containsKey(SUCCESS5));
        assertEquals(SUCCESS1, result.get(SUCCESS1));
        assertEquals(SUCCESS2, result.get(SUCCESS2));
        assertEquals(SUCCESS3, result.get(SUCCESS3));
        assertEquals(SUCCESS4, result.get(SUCCESS4));
        assertEquals(SUCCESS5, result.get(SUCCESS5));
    }

    @Test
    public void toMapWithValueMapper() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<Map<String, String>> promise = stream.toMap(String.class, String.class, new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return result;
            }
        }, new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return reverse(result);
            }
        });

        Map<String, String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.containsKey(SUCCESS1));
        assertTrue(result.containsKey(SUCCESS2));
        assertTrue(result.containsKey(SUCCESS3));
        assertTrue(result.containsKey(SUCCESS4));
        assertTrue(result.containsKey(SUCCESS5));
        assertEquals(reverse(SUCCESS1), result.get(SUCCESS1));
        assertEquals(reverse(SUCCESS2), result.get(SUCCESS2));
        assertEquals(reverse(SUCCESS3), result.get(SUCCESS3));
        assertEquals(reverse(SUCCESS4), result.get(SUCCESS4));
        assertEquals(reverse(SUCCESS5), result.get(SUCCESS5));
    }

    @Test
    public void toExistingMap() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Map<String, String> map = new HashMap<String, String>();
        Promise<Map<String, String>> promise = stream.toMap(map, new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return result;
            }
        });

        Map<String, String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.containsKey(SUCCESS1));
        assertTrue(result.containsKey(SUCCESS2));
        assertTrue(result.containsKey(SUCCESS3));
        assertTrue(result.containsKey(SUCCESS4));
        assertTrue(result.containsKey(SUCCESS5));
        assertEquals(SUCCESS1, result.get(SUCCESS1));
        assertEquals(SUCCESS2, result.get(SUCCESS2));
        assertEquals(SUCCESS3, result.get(SUCCESS3));
        assertEquals(SUCCESS4, result.get(SUCCESS4));
        assertEquals(SUCCESS5, result.get(SUCCESS5));
    }

    @Test
    public void toExistingMapWithValueMapper() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Map<String, String> map = new HashMap<String, String>();
        Promise<Map<String, String>> promise = stream.toMap(map, new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return result;
            }
        }, new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return reverse(result);
            }
        });

        Map<String, String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.containsKey(SUCCESS1));
        assertTrue(result.containsKey(SUCCESS2));
        assertTrue(result.containsKey(SUCCESS3));
        assertTrue(result.containsKey(SUCCESS4));
        assertTrue(result.containsKey(SUCCESS5));
        assertEquals(reverse(SUCCESS1), result.get(SUCCESS1));
        assertEquals(reverse(SUCCESS2), result.get(SUCCESS2));
        assertEquals(reverse(SUCCESS3), result.get(SUCCESS3));
        assertEquals(reverse(SUCCESS4), result.get(SUCCESS4));
        assertEquals(reverse(SUCCESS5), result.get(SUCCESS5));
    }

    @Test
    public void rejects() throws Throwable {
        PromiseStream<String> stream = createStream(true);
        Promise<String[]> promise = stream.toArray(String.class);

        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void filterNulls() throws Throwable {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(null);
        PromiseStream<String> stream = PromiseStream.from(promise1, promise2);

        Promise<String[]> promise = stream.filterNulls()
                .toArray(String.class);

        String[] result = assertResolves(promise);
        assertEquals(1, result.length);
        assertContainsAll(new String[]{SUCCESS1}, result);
    }

    @Test
    public void filterRejected() throws Throwable {
        PromiseStream<String> stream = createStream(true);
        Promise<String[]> promise = stream.filterRejected()
                .toArray(String.class);

        String[] result = assertResolves(promise);
        assertContainsAll(new String[]{SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4, SUCCESS5}, result);
    }

    @Test
    public void filterRejectedDoesNotHandle() throws Throwable {
        PromiseStream<String> stream = createStream(true);
        Promise<String[]> promise = stream.filterRejected(new OnRejectedHandler<Throwable, Boolean>() {
            @Override
            public Boolean handle(Throwable exception) throws Throwable {
                return false;
            }
        }).toArray(String.class);

        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void filterRejectedThrows() throws Throwable {
        PromiseStream<String> stream = createStream(true);
        Promise<String[]> promise = stream.filterRejected(new OnRejectedHandler<Throwable, Boolean>() {
            @Override
            public Boolean handle(Throwable exception) throws Throwable {
                throw exception;
            }
        }).toArray(String.class);

        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void typedFilterRejectedMismatch() throws Throwable {
        PromiseStream<String> stream = createStream(true);
        Promise<String[]> promise = stream.filterRejected(IllegalArgumentException.class)
                .toArray(String.class);

        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void filter() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.filter(new OnResolvedFunction<String, Boolean>() {
            @Override
            public Boolean resolved(String result) throws Throwable {
                return SUCCESS3.equals(result);
            }
        }).toArray(String.class);

        String[] result = assertResolves(promise);
        assertEquals(1, result.length);
        assertContainsAll(new String[]{SUCCESS3}, result);
    }

    @Test
    public void map() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.map(new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return reverse(result);
            }
        }).toArray(String.class);

        String[] result = assertResolves(promise);
        assertContainsAll(new String[]{
                reverse(SUCCESS1),
                reverse(SUCCESS2),
                reverse(SUCCESS3),
                reverse(SUCCESS4),
                reverse(SUCCESS5)
        }, result);
    }

    @Test
    public void mapThrows() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.map(new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                if (SUCCESS3.equals(result)) {
                    throw EXCEPTION;
                }
                return reverse(result);
            }
        }).toArray(String.class);

        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void flatMap() throws Throwable {
        Promise<List<String>> promise1 = resolveAfter(Arrays.asList(SUCCESS1, SUCCESS2, SUCCESS3), 10);
        Promise<List<String>> promise2 = resolveAfter(Arrays.asList(SUCCESS4, SUCCESS5), 10);
        PromiseStream<List<String>> stream = PromiseStream.from(promise1, promise2);

        PromiseStream<String> flattened = stream.flatMap(new OnResolvedFunction<List<String>, Iterable<String>>() {
            @Override
            public Iterable<String> resolved(List<String> result) throws Throwable {
                return result;
            }
        });

        Promise<List<String>> promise = flattened.toList(String.class);

        List<String> result = assertResolves(promise);
        assertEquals(5, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
        assertTrue(result.contains(SUCCESS5));
    }

    @Test
    public void compose() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.compose(new OnResolvedFunction<String, Future<String>>() {
            @Override
            public Future<String> resolved(String result) throws Throwable {
                return resolveAfter(reverse(result), 10);
            }
        }).toArray(String.class);

        String[] result = assertResolves(promise);
        assertContainsAll(new String[]{
                reverse(SUCCESS1),
                reverse(SUCCESS2),
                reverse(SUCCESS3),
                reverse(SUCCESS4),
                reverse(SUCCESS5)
        }, result);
    }

    @Test
    public void composeWithNullPromise() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.compose(new OnResolvedFunction<String, Future<String>>() {
            @Override
            public Future<String> resolved(String result) throws Throwable {
                if (SUCCESS3.equals(result)) {
                    return null;
                }
                return resolveAfter(result, 10);
            }
        }).toArray(String.class);

        String[] result = assertResolves(promise);
        assertEquals(5, result.length);
        assertContainsAll(new String[]{
                SUCCESS1,
                SUCCESS2,
                null,
                SUCCESS4,
                SUCCESS5
        }, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void composeWithRejectedPromise() throws Throwable {
        final Throwable exception = new Throwable();
        OnRejectedHandler<Throwable, Boolean> handler = mock(OnRejectedHandler.class);
        when(handler.handle(eq(exception))).thenReturn(true);

        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.compose(new OnResolvedFunction<String, Future<String>>() {
            @Override
            public Future<String> resolved(String result) throws Throwable {
                if (SUCCESS3.equals(result)) {
                    return rejectAfter(exception, 10);
                }
                return resolveAfter(result, 10);
            }
        }).filterRejected(handler).toArray(String.class);

        String[] result = assertResolves(promise);
        assertEquals(4, result.length);
        assertContainsAll(new String[]{
                SUCCESS1,
                SUCCESS2,
                SUCCESS4,
                SUCCESS5
        }, result);
        verify(handler, times(1)).handle(exception);
    }

    @Test
    public void groupingBy() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<Map<Integer, Set<String>>> promise = stream.collect(PromiseCollectors.groupingBy(Integer.class, String.class,
                new OnResolvedFunction<String, Integer>() {
                    @Override
                    public Integer resolved(String result) throws Throwable {
                        return result.length();
                    }
                }));

        Map<Integer, Set<String>> result = assertResolves(promise);
        assertTrue(result.containsKey(SUCCESS1.length()));
        assertEquals(5, result.get(SUCCESS1.length()).size());
    }

    @Test
    public void groupingByWithCollector() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<Map<Integer, String[]>> promise = stream.collect(PromiseCollectors.groupingBy(Integer.class,
                new OnResolvedFunction<String, Integer>() {
                    @Override
                    public Integer resolved(String result) throws Throwable {
                        return result.length();
                    }
                }, PromiseCollectors.toArray(String.class)));

        Map<Integer, String[]> result = assertResolves(promise);
        assertTrue(result.containsKey(SUCCESS1.length()));
        assertEquals(5, result.get(SUCCESS1.length()).length);
    }

    @Test
    public void groupingByWithCollectorFailsWithNonMultipleAccumulator() throws Throwable {
        final AtomicInteger counter = new AtomicInteger();
        PromiseStream<String> stream = createStream(false);

        Promise<Map<Integer, String[]>> promise = stream.collect(PromiseCollectors.groupingBy(Integer.class,
                new OnResolvedFunction<String, Integer>() {
                    @Override
                    public Integer resolved(String result) throws Throwable {
                        return counter.getAndIncrement();
                    }
                }, PromiseCollectors.toArray(new String[5])));

        assertRejects(IllegalStateException.class, promise);
    }

    @Test
    public void take() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.take(3).toArray(String.class);

        String[] result = assertResolves(promise);
        assertEquals(3, result.length);
    }

    @Test
    public void takeSmallerCount() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.take(10).toArray(String.class);

        String[] result = assertResolves(promise);
        assertEquals(5, result.length);
    }

    @Test
    public void takeWithRejected() throws Throwable {
        PromiseStream<String> stream = createStream(true);
        Promise<String[]> promise = stream.take(6).toArray(String.class);

        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void takeDuring() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 1000);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 1000);

        PromiseStream<String> stream = PromiseStream.from(promise1, promise2, promise3, promise4, promise5)
                .takeUntil(100, TimeUnit.MILLISECONDS);

        Promise<String[]> promise = stream.toArray(String.class);

        String[] result = assertResolves(promise);

        assertContainsAll(new String[] {
                SUCCESS1, SUCCESS2, SUCCESS3
        }, result);
    }

    @Test
    public void takeDuringWithRejected() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        Promise<String> promise3 = rejectAfter(EXCEPTION, 10);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 1000);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 1000);

        PromiseStream<String> stream = PromiseStream.from(promise1, promise2, promise3, promise4, promise5)
                .takeUntil(100, TimeUnit.MILLISECONDS);

        Promise<String[]> promise = stream.toArray(String.class);

        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void takeDuringCompleteBeforeTimeout() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 10);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 10);

        PromiseStream<String> stream = PromiseStream.from(promise1, promise2, promise3, promise4, promise5)
                .takeUntil(1000, TimeUnit.MILLISECONDS);

        Promise<String[]> promise = stream.toArray(String.class);

        String[] result = assertResolves(promise);

        assertContainsAll(new String[] {
                SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4, SUCCESS5
        }, result);
    }

    @Test(expected = IllegalStateException.class)
    @SuppressWarnings("unchecked")
    public void nullOperationFails() throws Throwable {
        TerminalOperator<String, String> operator = new TerminalOperator<String, String>() {
            @Override
            protected TerminalOperation<String, String> operation() {
                return null;
            }
        };

        PromiseStream<String> stream = mock(PromiseStream.class);

        Promise<String> ignore = operator.subscribe(stream);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void nullAccumulatorFails() throws Throwable {
        PromiseCollector<String, String, String> collector = mock(PromiseCollector.class);
        when(collector.getAccumulator()).thenReturn(null);

        Promise<String> promise = Promise.resolved(SUCCESS1);
        PromiseStream<String> stream = PromiseStream.from(promise);
        promise = stream.collect(collector);

        assertRejects(IllegalStateException.class, promise);
    }

    @Test(expected = IllegalStateException.class)
    public void PromiseCollectorsCannotBeCreated() throws Throwable {
        Class<PromiseCollectors> promiseCollectorsClass = PromiseCollectors.class;
        Constructor<?>[] constructors = promiseCollectorsClass.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        Constructor<?> constructor = constructors[0];
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        }
        catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }
}
