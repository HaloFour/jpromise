package org.jpromise;

import junit.framework.AssertionFailedError;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class PromiseHelpersTest {
    public static final String SUCCESS1 = "SUCCESS1";
    public static final String SUCCESS2 = "SUCCESS2";
    public static final Exception EXCEPTION1 = new ArithmeticException();
    public static final Exception EXCEPTION2 = new ConcurrentModificationException();

    @Test(expected = IllegalStateException.class)
    public void PromiseHelpersCannotBeCreated() throws Throwable {
        Class<PromiseHelpers> promiseHelpersClass = PromiseHelpers.class;
        Constructor<?>[] constructors = promiseHelpersClass.getDeclaredConstructors();
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
    public void resolveAfter() throws Throwable {
        Promise<String> promise = PromiseHelpers.resolveAfter(SUCCESS1, 10);
        assertFalse(promise.isDone());
        Thread.sleep(20);
        assertTrue(promise.isDone());
        String result = promise.get();
        assertEquals(SUCCESS1, result);
    }

    @Test
    public void resolveAfterWithDeferred() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        PromiseHelpers.resolveAfter(deferred, SUCCESS1, 10);
        assertFalse(promise.isDone());
        Thread.sleep(20);
        assertTrue(promise.isDone());
        String result = promise.get();
        assertEquals(SUCCESS1, result);
    }

    @Test
    public void rejectAfter() throws Throwable {
        Promise<String> promise = PromiseHelpers.rejectAfter(EXCEPTION1, 10);
        assertFalse(promise.isDone());
        Thread.sleep(20);
        assertTrue(promise.isDone());
        try {
            String ignored = promise.get();
            throw new AssertionFailedError("promise.get() should not return successfully");
        }
        catch (ExecutionException exception) {
            assertEquals(EXCEPTION1, exception.getCause());
        }
    }

    @Test
    public void rejectAfterWithDeferred() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        PromiseHelpers.rejectAfter(deferred, EXCEPTION1, 10);
        assertFalse(promise.isDone());
        Thread.sleep(20);
        assertTrue(promise.isDone());
        try {
            String ignored = promise.get();
            throw new AssertionFailedError("promise.get() should not return successfully");
        }
        catch (ExecutionException exception) {
            assertEquals(EXCEPTION1, exception.getCause());
        }
    }

    @Test
    public void assertResolves() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        String result = PromiseHelpers.assertResolves(promise);
        assertEquals(SUCCESS1, result);
    }

    @Test(expected = AssertionError.class)
    public void assertResolvesWithRejected() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        String ignored = PromiseHelpers.assertResolves(promise);
    }

    @Test
    public void assertResolvesWithTimeout() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        String result = PromiseHelpers.assertResolves(promise, 100L);
        assertEquals(SUCCESS1, result);
    }

    @Test(expected = TimeoutException.class)
    public void assertResolvesWithTimeoutTimesOut() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        String ignored = PromiseHelpers.assertResolves(promise, 100L);
        throw new AssertionFailedError("PromiseHelpers.assertResolves() should not return successfully");
    }

    @Test
    public void assertResolvesWithValue() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        String result = PromiseHelpers.assertResolves(SUCCESS1, promise);
        assertEquals(SUCCESS1, result);
    }

    @Test(expected = ComparisonFailure.class)
    public void assertResolvesWithValueMismatch() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        String ignored = PromiseHelpers.assertResolves(SUCCESS2, promise);
    }

    @Test
    public void assertResolvesWithTimeoutAndValue() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        String result = PromiseHelpers.assertResolves(SUCCESS1, promise, 100L);
        assertEquals(SUCCESS1, result);
    }

    @Test(expected = TimeoutException.class)
    public void assertResolvesWithTimeoutAndValueTimesOut() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        String ignored = PromiseHelpers.assertResolves(SUCCESS1, promise, 100L);
    }

    @Test(expected = ComparisonFailure.class)
    public void assertResolvesWithTimeoutAndValueMismatch() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        String ignored = PromiseHelpers.assertResolves(SUCCESS2, promise, 100L);
    }

    @Test
    public void assertRejects() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        Throwable exception = PromiseHelpers.assertRejects(promise);
        assertEquals(EXCEPTION1, exception);
    }

    @Test(expected = AssertionError.class)
    public void assertRejectsWithResolved() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        Throwable ignored = PromiseHelpers.assertRejects(promise);
    }

    @Test
    public void assertRejectsWithTimeout() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        Throwable exception = PromiseHelpers.assertRejects(promise, 100L);
        assertEquals(EXCEPTION1, exception);
    }

    @Test(expected = TimeoutException.class)
    public void assertRejectsWithTimeoutTimesOut() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        Throwable ignored = PromiseHelpers.assertRejects(promise, 100L);
        throw new AssertionFailedError("PromiseHelpers.assertRejects() should not return successfully");
    }

    @Test
    public void assertRejectsWithType() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        ArithmeticException exception = PromiseHelpers.assertRejects(ArithmeticException.class, promise);
        assertEquals(EXCEPTION1, exception);
    }

    @Test(expected = AssertionError.class)
    public void assertRejectsWithTypeMismatch() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        ConcurrentModificationException ignored = PromiseHelpers.assertRejects(ConcurrentModificationException.class, promise);
    }

    @Test
    public void assertRejectsWithValue() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        Throwable exception = PromiseHelpers.assertRejects(EXCEPTION1, promise);
        assertEquals(EXCEPTION1, exception);
    }

    @Test(expected = ComparisonFailure.class)
    public void assertRejectsWithValueMismatch() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        String ignored = PromiseHelpers.assertResolves(SUCCESS2, promise);
    }

    @Test
    public void assertRejectsWithTimeoutAndType() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        ArithmeticException exception = PromiseHelpers.assertRejects(ArithmeticException.class, promise, 100L);
        assertEquals(EXCEPTION1, exception);
    }

    @Test(expected = TimeoutException.class)
    public void assertRejectsWithTimeoutAndTypeTimesOut() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        ArithmeticException ignored = PromiseHelpers.assertRejects(ArithmeticException.class, promise, 100L);
    }

    @Test(expected = AssertionError.class)
    public void assertRejectsWithTimeoutAndTypeMismatch() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        ConcurrentModificationException ignored = PromiseHelpers.assertRejects(ConcurrentModificationException.class, promise, 100L);
    }

    @Test
    public void assertRejectsWithTimeoutAndValue() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        Throwable exception = PromiseHelpers.assertRejects(EXCEPTION1, promise, 100L);
        assertEquals(EXCEPTION1, exception);
    }

    @Test(expected = TimeoutException.class)
    public void assertRejectsWithTimeoutAndValueTimesOut() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        Throwable ignored = PromiseHelpers.assertRejects(EXCEPTION1, promise, 100L);
    }

    @Test(expected = AssertionError.class)
    public void assertRejectsWithTimeoutAndValueMismatch() throws Throwable {
        Promise<String> promise = Promise.rejected(EXCEPTION1);
        Throwable ignored = PromiseHelpers.assertRejects(EXCEPTION2, promise, 100L);
    }
}
