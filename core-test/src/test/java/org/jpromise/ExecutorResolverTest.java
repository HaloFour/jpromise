package org.jpromise;

import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;

public class ExecutorResolverTest {
    private static final String PROPERTY_KEY = PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR_KEY;

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

    public final static Executor executorField = PromiseExecutors.COMMON_POOL;
}
