package org.jpromise;

import org.jpromise.functions.FutureGenerator;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.jpromise.PromiseHelpers.assertFulfills;
import static org.jpromise.PromiseHelpers.assertRejects;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class PromiseStreamsTest {
    public static final String SUCCESS1 = "SUCCESS1";
    public static final String SUCCESS2 = "SUCCESS2";
    public static final String SUCCESS3 = "SUCCESS3";
    public static final String SUCCESS4 = "SUCCESS4";
    public static final String SUCCESS5 = "SUCCESS5";
    public static final String FAIL1 = "FAIL1";
    public static final Throwable EXCEPTION = new ArithmeticException(FAIL1);

    @Test(expected = IllegalStateException.class)
    public void PromiseStreamsCannotBeCreated() throws Throwable {
        Class<PromiseStreams> promiseStreamsClass = PromiseStreams.class;
        Constructor<?>[] constructors = promiseStreamsClass.getDeclaredConstructors();
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


    @Test
    public void from1() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.from(Promises.fulfilled(SUCCESS1));
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertFulfills(promise);
        assertEquals(1, result.size());
        assertTrue(result.contains(SUCCESS1));
    }

    @Test
    public void from2() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.from(
                Promises.fulfilled(SUCCESS1),
                Promises.fulfilled(SUCCESS2)
        );
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertFulfills(promise);
        assertEquals(2, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
    }

    @Test
    public void from3() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.from(
                Promises.fulfilled(SUCCESS1),
                Promises.fulfilled(SUCCESS2),
                Promises.fulfilled(SUCCESS3)
        );
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertFulfills(promise);
        assertEquals(3, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
    }

    @Test
    public void from4() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.from(
                Promises.fulfilled(SUCCESS1),
                Promises.fulfilled(SUCCESS2),
                Promises.fulfilled(SUCCESS3),
                Promises.fulfilled(SUCCESS4)
        );
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertFulfills(promise);
        assertEquals(4, result.size());
        assertTrue(result.contains(SUCCESS1));
        assertTrue(result.contains(SUCCESS2));
        assertTrue(result.contains(SUCCESS3));
        assertTrue(result.contains(SUCCESS4));
    }

    @Test
    public void from5() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.from(
                Promises.fulfilled(SUCCESS1),
                Promises.fulfilled(SUCCESS2),
                Promises.fulfilled(SUCCESS3),
                Promises.fulfilled(SUCCESS4),
                Promises.fulfilled(SUCCESS5)
        );
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertFulfills(promise);
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
        Promise<String>[] array = makeArray(Promises.fulfilled(SUCCESS1),
                Promises.fulfilled(SUCCESS2),
                Promises.fulfilled(SUCCESS3),
                Promises.fulfilled(SUCCESS4),
                Promises.fulfilled(SUCCESS5)
        );

        PromiseStream<String> stream = PromiseStreams.from(array);
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertFulfills(promise);
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
        PromiseStream<String> stream = PromiseStreams.from(iterable);
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertFulfills(promise);
        assertEquals(0, result.size());
    }

    @Test
    public void empty() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.empty(String.class);
        Promise<List<String>> promise = stream.toList(String.class);
        List<String> result = assertFulfills(promise);
        assertEquals(0, result.size());
    }

    @Test
    public void never() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.never(String.class);
        Promise<List<String>> promise = stream
                .takeUntil(100, TimeUnit.MILLISECONDS)
                .toList(String.class);
        List<String> result = assertFulfills(promise);
        assertEquals(0, result.size());
    }

    @Test
    public void generate() throws Throwable {
        FutureGenerator<Integer> generator = new FutureGenerator<Integer>() {
            @Override
            public Future<Integer> next(Integer result) {
                if (result == null) {
                    return Promises.fulfilled(0);
                }
                if (result > 4) {
                    return null;
                }
                return Promises.fulfilled(result + 1);
            }
        };

        generator = spy(generator);

        PromiseStream<Integer> stream = PromiseStreams.generate(generator);
        Promise<Integer[]> promise = stream.toArray(Integer.class);
        Integer[] result = assertFulfills(promise);
        assertArrayEquals(new Integer[] { 0, 1, 2, 3, 4, 5 }, result);
    }

    @Test
    public void generateGeneratorThrows() throws Throwable {
        FutureGenerator<Integer> generator = new FutureGenerator<Integer>() {
            @Override
            public Future<Integer> next(Integer result) {
                if (result == null) {
                    return Promises.fulfilled(0);
                }
                if (result > 4) {
                    throw new RuntimeException(EXCEPTION);
                }
                return Promises.fulfilled(result + 1);
            }
        };

        generator = spy(generator);

        PromiseStream<Integer> stream = PromiseStreams.generate(generator);
        Promise<Integer[]> promise = stream.toArray(Integer.class);
        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void generateGeneratorRejected() throws Throwable {
        FutureGenerator<Integer> generator = new FutureGenerator<Integer>() {
            @Override
            public Future<Integer> next(Integer result) {
                if (result == null) {
                    return Promises.fulfilled(0);
                }
                if (result > 4) {
                    return Promises.rejected(EXCEPTION);
                }
                return Promises.fulfilled(result + 1);
            }
        };

        generator = spy(generator);

        PromiseStream<Integer> stream = PromiseStreams.generate(generator);
        Promise<Integer[]> promise = stream.toArray(Integer.class);
        assertRejects(EXCEPTION, promise);
    }

    @Test
    public void single() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.single(SUCCESS1);

        Promise<String[]> promise = stream.toArray(String.class, 1);

        String[] result = assertFulfills(promise);
        assertArrayEquals(new String[] { SUCCESS1 }, result);
    }

    @Test
    public void rejected() throws Throwable {
        PromiseStream<String> stream = PromiseStreams.rejected(String.class, EXCEPTION);
        Promise<String[]> promise = stream.toArray(String.class, 0);
        assertRejects(EXCEPTION, promise);
    }
}
