package org.jpromise;

import org.jpromise.util.MessageUtil;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;

public class ExecutorResolverTest {
    private static final String PROPERTY_KEY = PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR_KEY;

    @Test(expected = IllegalStateException.class)
    public void ExecutorResolverCannotBeCreated() throws Throwable {
        Class<ExecutorResolver> executorResolverClass = ExecutorResolver.class;
        Constructor<?>[] constructors = executorResolverClass.getDeclaredConstructors();
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

    @Test(expected = IllegalStateException.class)
    public void MessageUtilCannotBeCreated() throws Throwable {
        Class<MessageUtil> messageUtilClass = MessageUtil.class;
        Constructor<?>[] constructors = messageUtilClass.getDeclaredConstructors();
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
    public void resolveCommonPool() throws Throwable {
        try (AutoCloseable ignore = setProperty(PROPERTY_KEY, PromiseExecutors.COMMON_POOL_KEY)) {
            Executor executor = ExecutorResolver.resolveBySetting(PROPERTY_KEY, null);
            assertEquals(PromiseExecutors.COMMON_POOL, executor);
        }
    }

    @Test
    public void resolveCurrentThread() throws Throwable {
        try (AutoCloseable ignore = setProperty(PROPERTY_KEY, PromiseExecutors.CURRENT_THREAD_KEY)) {
            Executor executor = ExecutorResolver.resolveBySetting(PROPERTY_KEY, null);
            assertEquals(PromiseExecutors.CURRENT_THREAD, executor);
        }
    }

    @Test
    public void resolveNewThread() throws Throwable {
        try (AutoCloseable ignore = setProperty(PROPERTY_KEY, PromiseExecutors.NEW_THREAD_KEY)) {
            Executor executor = ExecutorResolver.resolveBySetting(PROPERTY_KEY, null);
            assertEquals(PromiseExecutors.NEW_THREAD, executor);
        }
    }

    @Test
    public void resolveByClassName() throws Throwable {
        try (AutoCloseable ignore = setProperty(PROPERTY_KEY, "java.util.concurrent.ForkJoinPool")) {
            Executor executor = ExecutorResolver.resolveBySetting(PROPERTY_KEY, null);
            assertNotNull(executor);
            assertTrue(ForkJoinPool.class.isAssignableFrom(executor.getClass()));
        }
    }

    @Test
    public void resolveByClassNameThrows() throws Throwable {
        try (AutoCloseable ignore = setProperty(PROPERTY_KEY, "java.util.concurrent.ForkJoinFool")) {
            Executor executor = ExecutorResolver.resolveBySetting(PROPERTY_KEY, PromiseExecutors.COMMON_POOL);
            assertNotNull(executor);
            assertEquals(PromiseExecutors.COMMON_POOL, executor);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByClassNameNull() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByClassNameNotExecutor() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByClassName("java.lang.String");
    }

    @Test(expected = ClassNotFoundException.class)
    public void resolveByClassNameNotFound() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByClassName("java.lang.SillyString");
    }

    @Test(expected = InstantiationException.class)
    public void resolveByClassCannotInstantiate() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByClassName("org.jpromise.PromiseExecutors");
    }

    @Test
    public void resolveByFieldName() throws Throwable {
        try (AutoCloseable ignore = setProperty(PROPERTY_KEY, "org.jpromise.ExecutorResolverTest#executorField")) {
            Executor executor = ExecutorResolver.resolveBySetting(PROPERTY_KEY, null);
            assertEquals(PromiseExecutors.COMMON_POOL, executor);
        }
    }

    @Test
    public void resolveByMethodName() throws Throwable {
        try (AutoCloseable ignore = setProperty(PROPERTY_KEY, "org.jpromise.ExecutorResolverTest#executorMethod()")) {
            Executor executor = ExecutorResolver.resolveBySetting(PROPERTY_KEY, null);
            assertEquals(PromiseExecutors.COMMON_POOL, executor);
        }
    }

    @Test(expected = NoSuchMethodException.class)
    public void resolveByMethodNameNotPublic() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByName("org.jpromise.ExecutorResolverTest#nonPublicMethod()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByMethodNameNotStatic() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByName("org.jpromise.ExecutorResolverTest#nonStaticMethod()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByMethodNameNotExecutor() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByName("org.jpromise.ExecutorResolverTest#nonExecutorMethod()");
    }

    @Test(expected = NoSuchFieldException.class)
    public void resolveByFieldNameNotPublic() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByName("org.jpromise.ExecutorResolverTest#nonPublicField");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByFieldNameNotStatic() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByName("org.jpromise.ExecutorResolverTest#nonStaticField");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveByFieldNameNotExecutor() throws Throwable {
        Executor ignored = ExecutorResolver.resolveByName("org.jpromise.ExecutorResolverTest#nonExecutorField");
    }

    private static AutoCloseable setProperty(final String key, String value) {
        final String previous = System.getProperty(key);
        System.setProperty(key, value);
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                if (previous != null) {
                    System.setProperty(key, previous);
                }
                else {
                    System.clearProperty(key);
                }
            }
        };
    }

    public static Executor executorMethod() {
        return PromiseExecutors.COMMON_POOL;
    }
    public static String nonExecutorMethod() { return nonExecutorField; }
    public Executor nonStaticMethod() { return PromiseExecutors.NEW_THREAD; }
    protected static Executor nonPublicMethod() { return PromiseExecutors.NEW_THREAD; }

    public final static Executor executorField = PromiseExecutors.COMMON_POOL;
    public final static String nonExecutorField = "NON_EXECUTOR";
    public final Executor nonStaticField = PromiseExecutors.NEW_THREAD;
    protected static final Executor nonPublicField = PromiseExecutors.NEW_THREAD;
}
