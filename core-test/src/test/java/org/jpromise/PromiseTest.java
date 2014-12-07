package org.jpromise;

import junit.framework.AssertionFailedError;
import org.jpromise.functions.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.*;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PromiseTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String FAIL1 = "FAIL1";

    @Test
    public void pending() {
        Deferred<String> deferred = new DeferredPromise<>();
        Promise<String> promise = deferred.promise();
        assertEquals(PromiseState.PENDING, promise.state());
        assertFalse(promise.isResolved());
        assertFalse(promise.isRejected());
        assertFalse(promise.isDone());
        assertFalse(promise.isCancelled());
        assertEquals(promise.toString(), "[PENDING]");
    }

    @Test
    public void resolved() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);

        assertResolves(SUCCESS1, promise);
        assertEquals("[RESOLVED]: SUCCESS1", promise.toString());
    }

    @Test
    public void rejected() throws Throwable {
        Exception exception = new Exception(FAIL1);
        Promise<String> promise = Promise.rejected(exception);

        assertRejects(exception, promise);
        assertEquals("[REJECTED]: " + exception.toString(), promise.toString());
    }

    @Test
    public void resolves() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 100);

        assertEquals(promise.state(), PromiseState.PENDING);

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void rejects() throws Throwable {
        final Exception exception = new Exception();
        Promise<String> promise = rejectAfter(exception, 100);

        assertRejects(exception, promise);
    }

    @Test
    public void then() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        @SuppressWarnings("unchecked")
        OnResolved<String> callback = mock(OnResolved.class);

        Promise<String> promise2 = promise1.then(callback);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS1, promise2);
        verify(callback, times(1)).resolved(SUCCESS1);
    }

    @Test
    public void thenRejected() throws Throwable {
        Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);

        @SuppressWarnings("unchecked")
        OnResolved<String> callback = mock(OnResolved.class);
        Promise<String> promise2 = promise1.then(callback);

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);
        verify(callback, never()).resolved(anyString());
    }

    @Test
    public void thenThrows() throws Throwable {
        final Exception exception = new Exception();
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = promise1.then(new OnResolved<String>() {
            @Override
            public void resolved(String result) throws Throwable {
                throw exception;
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertRejects(exception, promise2);
    }

    @Test
    public void testApply() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = promise1.thenApply(new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return SUCCESS2;
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
    }

    @Test
    public void testApplyRejected() throws Throwable {
        Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);

        @SuppressWarnings("unchecked")
        OnResolvedFunction<String, String> callback = mock(OnResolvedFunction.class);
        when(callback.resolved(anyString())).thenReturn(SUCCESS2);

        Promise<String> promise2 = promise1.thenApply(callback);

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);

        verify(callback, never()).resolved(anyString());
    }

    @Test
    public void thenApplyThrows() throws Throwable {
        final Exception exception = new Exception();
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = promise1.thenApply(new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                throw exception;
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertRejects(exception, promise2);
    }

    @Test
    public void thenCompose() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        Promise<String> promise2 = promise1.thenCompose(new OnResolvedFunction<String, Future<String>>() {
            @Override
            public Future<String> resolved(String d) {
                return resolveAfter(SUCCESS2, 10);
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
    }

    @Test
    public void thenComposeRejected() throws Throwable {
        Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);

        Promise<String> promise2 = promise1.thenCompose(new OnResolvedFunction<String, Promise<String>>() {
            @Override
            public Promise<String> resolved(String d) {
                return resolveAfter(SUCCESS1, 10);
            }
        });

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);
    }

    @Test
    public void thenComposeRejects() throws Throwable {
        final Exception exception = new Exception();
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        Promise<String> promise2 = promise1.thenCompose(new OnResolvedFunction<String, Promise<String>>() {
            @Override
            public Promise<String> resolved(String d) {
                return rejectAfter(exception, 10);
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertRejects(exception, promise2);
    }

    @Test
    public void thenComposeThrows() throws Throwable {
        final Exception exception = new Exception();
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        Promise<String> promise2 = promise1.thenCompose(new OnResolvedFunction<String, Promise<String>>() {
            @Override
            public Promise<String> resolved(String d) throws Throwable {
                throw exception;
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertRejects(exception, promise2);
    }

    @Test
    public void thenComposeNullPromise() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        Promise<String> promise2 = promise1.thenCompose(new OnResolvedFunction<String, Promise<String>>() {
            @Override
            public Promise<String> resolved(String d) throws Throwable {
                return null;
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertResolves(null, promise2);
    }

    @Test
    public void fail() throws Throwable {
        Exception exception = new TimeoutException();
        Promise<String> promise1 = Promise.rejected(exception);

        @SuppressWarnings("unchecked")
        OnRejected<Throwable> callback = mock(OnRejected.class);
        Promise<String> promise2 = promise1.rejected(callback);

        assertRejects(exception, promise1);
        verify(callback, times(1)).rejected(exception);
        assertRejects(exception, promise2);
    }

    @Test
    public void typedFail() throws Throwable {
        Exception exception = new TimeoutException();
        Promise<String> promise1 = Promise.rejected(exception);

        @SuppressWarnings("unchecked")
        OnRejected<Throwable> callback = mock(OnRejected.class);

        Promise<String> promise2 = promise1.rejected(TimeoutException.class, callback);

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);

        verify(callback, times(1)).rejected(exception);
    }

    @Test
    public void typedFailMismatch() throws Throwable {
        Exception exception = new TimeoutException();
        Promise<String> promise1 = Promise.rejected(exception);

        @SuppressWarnings("unchecked")
        OnRejected<Throwable> callback = mock(OnRejected.class);

        Promise<String> promise2 = promise1.rejected(IllegalArgumentException.class, callback);

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);

        verify(callback, never()).rejected(any(Throwable.class));
    }

    @Test
    public void failResolved() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        OnRejectedHandler<Throwable, String> callback = mock(OnRejectedHandler.class);
        Promise<String> promise2 = promise1.handleWith(callback);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS1, promise2);
        verify(callback, never()).handle(any(Throwable.class));
    }

    @Test
    public void failThrows() throws Throwable {
        Exception exception1 = new TimeoutException();
        final Exception exception2 = new IllegalArgumentException();
        Promise<String> promise1 = Promise.rejected(exception1);

        Promise<String> promise2 = promise1.rejected(new OnRejected<Throwable>() {
            @Override
            public void rejected(Throwable reason) throws Throwable {
                throw exception2;
            }
        });

        assertRejects(exception1, promise1);
        assertRejects(exception2, promise2);
    }

    @Test
    public void handle() throws Throwable {
        Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);
        Promise<String> promise2 = promise1.handleWith(new OnRejectedHandler<Throwable, String>() {
            @Override
            public String handle(Throwable exception) {
                return SUCCESS1;
            }
        });

        assertRejects(exception, promise1);
        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void typedHandle() throws Throwable {
        Exception exception = new ArithmeticException();
        Promise<String> promise1 = rejectAfter(exception, 10);
        Promise<String> promise2 = promise1.handleWith(ArithmeticException.class, new OnRejectedHandler<ArithmeticException, String>() {
            @Override
            public String handle(ArithmeticException exception) {
                return SUCCESS1;
            }
        });

        assertRejects(exception, promise1);
        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void typedHandleMismatch() throws Throwable {
        Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);
        Promise<String> promise2 = promise1.handleWith(ArithmeticException.class, new OnRejectedHandler<ArithmeticException, String>() {
            @Override
            public String handle(ArithmeticException exception) {
                return SUCCESS1;
            }
        });

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);
    }

    @Test
    public void handleResolved() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = promise1.handleWith(new OnRejectedHandler<Throwable, String>() {
            @Override
            public String handle(Throwable exception) {
                return SUCCESS2;
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void handleThrows() throws Throwable {
        Exception exception1 = new Exception();
        final Exception exception2 = new Exception();
        Promise<String> promise1 = rejectAfter(exception1, 10);
        Promise<String> promise2 = promise1.handleWith(new OnRejectedHandler<Throwable, String>() {
            @Override
            public String handle(Throwable reason) throws Throwable {
                throw exception2;
            }
        });

        assertRejects(exception1, promise1);
        assertRejects(exception2, promise2);
    }

    @Test
    public void fallback() throws Throwable {
        Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);

        Promise<String> promise2 = promise1.fallbackWith(new OnRejectedHandler<Throwable, Promise<String>>() {
            @Override
            public Promise<String> handle(Throwable reason) throws Throwable {
                return resolveAfter(SUCCESS1, 10);
            }
        });

        assertRejects(exception, promise1);
        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void fallbackResolved() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        Promise<String> promise2 = promise1.fallbackWith(new OnRejectedHandler<Throwable, Promise<String>>() {
            @Override
            public Promise<String> handle(Throwable reason) {
                return resolveAfter(SUCCESS2, 10);
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void fallbackRejects() throws Throwable {
        final Exception exception1 = new Exception();
        final Exception exception2 = new Exception();
        Promise<String> promise1 = rejectAfter(exception1, 10);

        Promise<String> promise2 = promise1.fallbackWith(new OnRejectedHandler<Throwable, Promise<String>>() {
            @Override
            public Promise<String> handle(Throwable reason) {
                return rejectAfter(exception2, 10);
            }
        });

        assertRejects(exception1, promise1);
        assertRejects(exception2, promise2);
    }

    @Test
    public void fallbackThrows() throws Throwable {
        final Exception exception1 = new Exception();
        final Exception exception2 = new Exception();
        Promise<String> promise1 = rejectAfter(exception1, 10);

        Promise<String> promise2 = promise1.fallbackWith(new OnRejectedHandler<Throwable, Promise<String>>() {
            @Override
            public Promise<String> handle(Throwable reason) throws Throwable {
                throw exception2;
            }
        });

        assertRejects(exception1, promise1);
        assertRejects(exception2, promise2);
    }

    @Test
    public void fallbackNullPromise() throws Throwable {
        final Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);

        Promise<String> promise2 = promise1.fallbackWith(new OnRejectedHandler<Throwable, Promise<String>>() {
            @Override
            public Promise<String> handle(Throwable reason) {
                return null;
            }
        });

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);
    }

    @Test
    public void typedFallback() throws Throwable {
        final Exception exception = new ArithmeticException();
        Promise<String> promise1 = rejectAfter(exception, 10);

        Promise<String> promise2 = promise1.fallbackWith(ArithmeticException.class, new OnRejectedHandler<ArithmeticException, Promise<String>>() {
            @Override
            public Promise<String> handle(ArithmeticException reason) {
                return resolveAfter(SUCCESS1, 10);
            }
        });

        assertRejects(exception, promise1);
        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void typedFallbackMismatch() throws Throwable {
        final Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);

        Promise<String> promise2 = promise1.fallbackWith(ArithmeticException.class, new OnRejectedHandler<ArithmeticException, Promise<String>>() {
            @Override
            public Promise<String> handle(ArithmeticException reason) {
                return resolveAfter(SUCCESS1, 10);
            }
        });

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);
    }

    @Test
    public void cancel() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 100);
        assertTrue(promise.cancel(true));
        assertRejects(CancellationException.class, promise);
        assertTrue(promise.isCancelled());
    }

    @Test
    public void get() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 10);
        String result = promise.get();
        assertEquals(SUCCESS1, result);
    }

    @Test(expected = ExecutionException.class)
    public void getRejected() throws Throwable {
        Promise<String> promise = rejectAfter(new ArithmeticException(), 10);
        String ignored = promise.get();
        throw new AssertionFailedError("promise.get() should not return successfully");
    }

    @Test
    public void getTimed() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 10);
        String result = promise.get(100, TimeUnit.MILLISECONDS);
        assertEquals(SUCCESS1, result);
    }

    @Test(expected = ExecutionException.class)
    public void getTimedRejected() throws Throwable {
        Promise<String> promise = rejectAfter(new ArithmeticException(), 10);
        String ignored = promise.get(100, TimeUnit.MILLISECONDS);
        throw new AssertionFailedError("promise.get(long, TimeUnit) should not return successfully");
    }

    @Test(expected = TimeoutException.class)
    public void getTimedTimesOut() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 1000);
        String ignored = promise.get(10, TimeUnit.MILLISECONDS);
        throw new AssertionFailedError("promise.get(long, TimeUnit) should not return successfully");
    }
}
