package org.jpromise;

import org.jpromise.Deferred;
import org.jpromise.DeferredPromise;
import org.jpromise.Promise;
import org.jpromise.PromiseState;
import org.jpromise.functions.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class PromiseHelpers {
    private static class PromiseResult<R> {
        public R result;
        public Throwable reason;
    }

    public static final long DEFAULT_TIMEOUT = 2000L;

    public static <T> void resolveAfter(final Deferred<T> deferred, final T value, long delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                deferred.resolve(value);
            }
        }, delay);
    }

    public static <T> void rejectAfter(final Deferred<T> deferred, final Throwable throwable, long delay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                deferred.reject(throwable);
            }
        }, delay);
    }

    public static <T> Promise<T> resolveAfter(final T value, long delay) {
        Deferred<T> deferred = new DeferredPromise<T>();
        resolveAfter(deferred, value, delay);
        return deferred.promise();
    }

    public static <T> Promise<T> rejectAfter(final Throwable throwable, long delay) {
        Deferred<T> deferred = new DeferredPromise<T>();
        rejectAfter(deferred, throwable, delay);
        return deferred.promise();
    }

    private static <T> PromiseResult<T> await(Promise<T> promise, long millis) throws InterruptedException, TimeoutException {
        final PromiseResult<T> promiseResult = new PromiseResult<T>();
        final CountDownLatch latch = new CountDownLatch(1);
        promise.whenCompleted(new OnCompleted<T>() {
            @Override
            public void completed(Promise<T> promise, T result, Throwable exception) throws Throwable {
                switch (promise.state()) {
                    case RESOLVED:
                        promiseResult.result = result;
                        break;
                    case REJECTED:
                        promiseResult.reason = exception;
                        break;
                }
                latch.countDown();
            }
        });
        if (!latch.await(millis, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("The promise did not complete within the specified duration.");
        }
        return promiseResult;
    }

    public static <T> T assertResolves(Promise<T> promise) throws InterruptedException, ExecutionException, TimeoutException {
        return assertResolves(promise, DEFAULT_TIMEOUT);
    }

    public static <T> T assertResolves(Promise<T> promise, long millis) throws InterruptedException, ExecutionException, TimeoutException {
        PromiseResult<T> promiseResult = await(promise, millis);
        assertEquals(PromiseState.RESOLVED, promise.state());
        assertTrue(promise.isDone());
        assertTrue(promise.isResolved());
        assertFalse(promise.isRejected());
        return promiseResult.result;
    }

    public static <T> T assertResolves(T expected, Promise<T> promise) throws InterruptedException, ExecutionException, TimeoutException {
        T result = assertResolves(promise);
        assertEquals(expected, result);
        return result;
    }

    public static <T> T assertResolves(T expected, Promise<T> promise, long millis) throws InterruptedException, ExecutionException, TimeoutException {
        T result = assertResolves(promise, millis);
        assertEquals(expected, result);
        return result;
    }

    public static <T> Throwable assertRejects(Promise<T> promise) throws InterruptedException, TimeoutException {
        return assertRejects(promise, DEFAULT_TIMEOUT);
    }

    public static <T> Throwable assertRejects(Promise<T> promise, long millis) throws InterruptedException, TimeoutException {
        PromiseResult<T> promiseResult = await(promise, millis);
        assertEquals(PromiseState.REJECTED, promise.state());
        assertTrue(promise.isDone());
        assertFalse(promise.isResolved());
        assertTrue(promise.isRejected());
        return promiseResult.reason;
    }

    public static <T, E extends Throwable> E assertRejects(Class<E> expectedClass, Promise<T> promise) throws InterruptedException, TimeoutException {
        Throwable reason = assertRejects(promise, DEFAULT_TIMEOUT);
        assertTrue(expectedClass.isInstance(reason));
        return expectedClass.cast(reason);
    }

    public static <T, E extends Throwable> E assertRejects(Class<E> expectedClass, Promise<T> promise, long millis) throws InterruptedException, TimeoutException {
        Throwable reason = assertRejects(promise, millis);
        assertTrue(expectedClass.isInstance(reason));
        return expectedClass.cast(reason);
    }

    public static <T, E extends Throwable> E assertRejects(E expected, Promise<T> promise) throws InterruptedException, TimeoutException {
        Throwable reason = assertRejects(promise, DEFAULT_TIMEOUT);
        assertEquals(expected, reason);
        return expected;
    }

    public static <T, E extends Throwable> E assertRejects(E expected, Promise<T> promise, long millis) throws InterruptedException, TimeoutException {
        Throwable reason = assertRejects(promise, millis);
        assertEquals(expected, reason);
        return expected;
    }
}
