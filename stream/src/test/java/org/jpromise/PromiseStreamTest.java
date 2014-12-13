package org.jpromise;

import junit.framework.AssertionFailedError;
import org.jpromise.functions.OnResolvedFunction;
import org.jpromise.operators.TerminalOperation;
import org.jpromise.operators.TerminalOperator;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PromiseStreamTest {
    public static final String SUCCESS1 = "SUCCESS1";
    public static final String SUCCESS2 = "SUCCESS2";
    public static final String SUCCESS3 = "SUCCESS3";
    public static final String SUCCESS4 = "SUCCESS4";
    public static final String SUCCESS5 = "SUCCESS5";
    public static final String FAIL1 = "FAIL1";
    public static final Throwable EXCEPTION = new Exception(FAIL1);

    private PromiseStream<String> createStream(boolean includeRejection) {
        List<Promise<String>> promises = new ArrayList<>(6);
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
            if (value.equals(value)) {
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
    public void toArray() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.toArray(String.class);

        String[] result = assertResolves(promise);

        assertContainsAll(new String[] { SUCCESS1, SUCCESS2, SUCCESS3, SUCCESS4, SUCCESS5 }, result);
    }

    @Test
    public void toArrayOutOfBounds() throws Throwable {
        PromiseStream<String> stream = createStream(false);
        Promise<String[]> promise = stream.toArray(new String[4]);
        assertRejects(IndexOutOfBoundsException.class, promise);
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

    @Test(expected = IllegalStateException.class)
    public void nullOperationFails() throws Throwable {
        @SuppressWarnings("unchecked")
        OnSubscribe<String> subscribe = mock(OnSubscribe.class);
        TerminalOperator<String, String> operator = new TerminalOperator<String, String>(subscribe) {
            @Override
            protected TerminalOperation<String, String> operation() {
                return null;
            }
        };

        Promise<String> ignore = operator.subscribe();
    }

    @Test
    public void nullAccumulatorFails() throws Throwable {
        @SuppressWarnings("unchecked")
        OnSubscribe<String> subscribe = mock(OnSubscribe.class);
        @SuppressWarnings("unchecked")
        PromiseCollector<String, String, String> collector = mock(PromiseCollector.class);
        when(collector.getAccumulator()).thenReturn(null);

        Promise<String> promise = Promise.resolved(SUCCESS1);
        PromiseStream<String> stream = PromiseStream.from(promise);
        promise = stream.collect(collector);

        assertRejects(IllegalStateException.class, promise);
    }
}
