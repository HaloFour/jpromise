package org.jpromise;

import junit.framework.AssertionFailedError;
import org.jpromise.functions.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
    public void defer() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        assertFalse(promise.isDone());
    }

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
    public void resolvedVoid() throws Throwable {
        Promise<Void> promise = Promise.resolved();
        assertResolves(promise);
    }

    @Test
    public void rejected() throws Throwable {
        Exception exception = new Exception(FAIL1);
        Promise<String> promise = Promise.rejected(exception);

        assertRejects(exception, promise);
        assertEquals("[REJECTED]: " + exception.toString(), promise.toString());
    }

    @Test
    public void typedRejected() throws Throwable {
        Exception exception = new Exception(FAIL1);
        Promise<String> promise = Promise.rejected(String.class, exception);

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
    public void thenComposeFuture() throws Throwable {
        final ForkJoinPool pool = new ForkJoinPool();
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        Promise<String> promise2 = promise1.thenCompose(new OnResolvedFunction<String, Future<String>>() {
            @Override
            public Future<String> resolved(String result) throws Throwable {
                return pool.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return SUCCESS2;
                    }
                });
            }
        });

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
    }

    @Test
    public void whenRejectedPromise() throws Throwable {
        Exception exception = new TimeoutException();
        Promise<String> promise1 = Promise.rejected(exception);

        @SuppressWarnings("unchecked")
        OnRejected<Throwable> callback = mock(OnRejected.class);
        Promise<String> promise2 = promise1.whenRejected(callback);

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);
        verify(callback, times(1)).rejected(exception);
    }

    @Test
    public void typedWhenRejected() throws Throwable {
        Exception exception = new TimeoutException();
        Promise<String> promise1 = Promise.rejected(exception);

        @SuppressWarnings("unchecked")
        OnRejected<Throwable> callback = mock(OnRejected.class);

        Promise<String> promise2 = promise1.whenRejected(TimeoutException.class, callback);

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);

        verify(callback, times(1)).rejected(exception);
    }

    @Test
    public void typedWhenRejectedMismatch() throws Throwable {
        Exception exception = new TimeoutException();
        Promise<String> promise1 = Promise.rejected(exception);

        @SuppressWarnings("unchecked")
        OnRejected<Throwable> callback = mock(OnRejected.class);

        Promise<String> promise2 = promise1.whenRejected(IllegalArgumentException.class, callback);

        assertRejects(exception, promise1);
        assertRejects(exception, promise2);

        verify(callback, never()).rejected(any(Throwable.class));
    }

    @Test
    public void whenRejectedResolved() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        @SuppressWarnings("unchecked")
        OnRejected<Throwable> callback = mock(OnRejected.class);
        Promise<String> promise2 = promise1.whenRejected(callback);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS1, promise2);
        verify(callback, never()).rejected(any(Throwable.class));
    }

    @Test
    public void whenRejectedThrows() throws Throwable {
        Exception exception1 = new TimeoutException();
        final Exception exception2 = new IllegalArgumentException();
        Promise<String> promise1 = Promise.rejected(exception1);

        Promise<String> promise2 = promise1.whenRejected(new OnRejected<Throwable>() {
            @Override
            public void rejected(Throwable reason) throws Throwable {
                throw exception2;
            }
        });

        assertRejects(exception1, promise1);
        assertRejects(exception2, promise2);
    }

    @Test
    public void whenRejectedWithExecutor() throws Throwable {
        @SuppressWarnings("unchecked")
        OnRejected<Throwable> callback = mock(OnRejected.class);
        Executor executor = mock(Executor.class);
        Throwable exception = new Exception();

        Promise<String> promise1 = rejectAfter(exception, 10);
        Promise<String> promise2 = promise1.whenRejected(executor, callback);

        assertRejects(exception, promise1);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());

        Runnable runnable = captor.getValue();
        runnable.run();
        verify(callback, times(1)).rejected(exception);

        assertRejects(exception, promise2);
    }

    @Test
    public void handleWith() throws Throwable {
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
    public void handleWithResult() throws Throwable {
        Exception exception = new Exception();
        Promise<String> promise1 = rejectAfter(exception, 10);
        Promise<String> promise2 = promise1.handleWith(SUCCESS1);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void typedHandleWith() throws Throwable {
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
    public void typedHandleWithResult() throws Throwable {
        Exception exception = new ArithmeticException();
        Promise<String> promise1 = rejectAfter(exception, 10);
        Promise<String> promise2 = promise1.handleWith(ArithmeticException.class, SUCCESS1);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void typedHandleWithMismatch() throws Throwable {
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
    public void handleWithResolved() throws Throwable {
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
    public void handleWithThrows() throws Throwable {
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
    public void handleWithWithExecutor() throws Throwable {
        Throwable exception = new Exception();
        @SuppressWarnings("unchecked")
        OnRejectedHandler<Throwable, String> callback = mock(OnRejectedHandler.class);
        when(callback.handle(exception)).thenReturn(SUCCESS1);
        Executor executor = mock(Executor.class);

        Promise<String> promise1 = rejectAfter(exception, 10);
        Promise<String> promise2 = promise1.handleWith(executor, callback);

        assertRejects(exception, promise1);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());

        Runnable runnable = captor.getValue();
        runnable.run();
        verify(callback, times(1)).handle(exception);

        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void fallbackWith() throws Throwable {
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
    public void fallbackWithResolved() throws Throwable {
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
    public void fallbackWithRejects() throws Throwable {
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
    public void fallbackWithThrows() throws Throwable {
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
    public void fallbackWithNullPromise() throws Throwable {
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
    public void fallbackWithWithExecutor() throws Throwable {
        Throwable exception = new Exception();
        @SuppressWarnings("unchecked")
        OnRejectedHandler<Throwable, Future<String>> callback = mock(OnRejectedHandler.class);
        when(callback.handle(exception)).thenReturn(Promise.resolved(SUCCESS1));
        Executor executor = mock(Executor.class);

        Promise<String> promise1 = rejectAfter(exception, 10);
        Promise<String> promise2 = promise1.fallbackWith(executor, callback);

        assertRejects(exception, promise1);
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());
        reset(executor);

        Runnable runnable = captor.getValue();
        runnable.run();

        verify(callback, times(1)).handle(exception);
        verify(executor, times(1)).execute(captor.capture());
        runnable = captor.getValue();
        runnable.run();

        assertResolves(SUCCESS1, promise2);
    }

    @Test
    public void typedFallbackWith() throws Throwable {
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
    public void typedFallbackWithMismatch() throws Throwable {
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
    public void executorThrows() throws Throwable {
        Throwable exception = new RuntimeException();
        Executor executor = mock(Executor.class);
        doThrow(exception).when(executor).execute(any(Runnable.class));

        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = promise1.thenApply(executor, new OnResolvedFunction<String, String>() {
            @Override
            public String resolved(String result) throws Throwable {
                return SUCCESS2;
            }
        });

        assertResolves(SUCCESS1, promise1);
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
    public void cancelCompleted() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        assertFalse(promise.cancel(true));
        assertResolves(SUCCESS1, promise);
        assertFalse(promise.isCancelled());
    }

    @Test
    public void cancelPreventsCallback() throws Throwable {
        @SuppressWarnings("unchecked")
        OnResolved<String> callback = mock(OnResolved.class);

        Deferred<String> deferred = Promise.defer();
        Promise<String> promise1 = deferred.promise();
        Promise<String> promise2 = promise1.then(callback);

        assertTrue(promise2.cancel(true));
        deferred.resolve(SUCCESS1);

        assertResolves(SUCCESS1, promise1);
        assertRejects(CancellationException.class, promise2);
        verify(callback, never()).resolved(anyString());
    }

    @Test
    public void cancelPreventsSubmittedCallback() throws Throwable {
        Executor executor = mock(Executor.class);
        @SuppressWarnings("unchecked")
        OnResolved<String> callback = mock(OnResolved.class);

        Deferred<String> deferred = Promise.defer();
        Promise<String> promise1 = deferred.promise();
        Promise<String> promise2 = promise1.then(executor, callback);

        deferred.resolve(SUCCESS1);
        assertTrue(promise2.cancel(true));

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();

        assertResolves(SUCCESS1, promise1);
        assertRejects(CancellationException.class, promise2);
        verify(callback, never()).resolved(anyString());
    }

    @Test
    public void cancelChained() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise1 = deferred.promise();

        @SuppressWarnings("unchecked")
        OnResolved<String> callback = mock(OnResolved.class);

        Promise<String> promise2 = promise1.then(callback);

        promise2.cancel(true);
        deferred.resolve(SUCCESS1);

        assertRejects(CancellationException.class, promise2);
        assertResolves(SUCCESS1, promise1);
        verify(callback, never()).resolved(anyString());
    }

    @Test
    public void cancelComposedCancelsPromise() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise1 = deferred.promise();

        @SuppressWarnings("unchecked")
        OnResolvedFunction<String, Future<String>> callback = mock(OnResolvedFunction.class);
        @SuppressWarnings("unchecked")
        Promise<String> future = mock(Promise.class);

        when(callback.resolved(anyString())).thenReturn(future);
        when(future.whenCompleted(any(Executor.class), Mockito.<OnCompleted<String>>any())).thenReturn(null);
        when(future.cancel(anyBoolean())).thenReturn(true);

        Promise<String> promise2 = promise1.thenCompose(callback);

        deferred.resolve(SUCCESS1);
        assertResolves(SUCCESS1, promise1);
        verify(future, timeout(10).times(1)).whenCompleted(any(Executor.class), Mockito.<OnCompleted<String>>any());

        promise2.cancel(true);
        assertRejects(CancellationException.class, promise2);
        verify(future, timeout(100).times(1)).cancel(anyBoolean());
    }

    @Test
    public void cancelComposedAlreadyComplete() throws Throwable {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = promise1.then(PromiseExecutors.CURRENT_THREAD, new OnResolved<String>() {
            @Override
            public void resolved(String result) throws Throwable {
            }
        });

        assertFalse(promise2.cancel(true));
        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS1, promise2);
        assertFalse(promise2.isCancelled());
    }

    @Test
    public void cancelAfter() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 100);
        Promise<Boolean> cancel = promise.cancelAfter(true, 10, TimeUnit.MILLISECONDS);

        assertResolves(true, cancel);
        assertRejects(CancellationException.class, promise);
    }

    @Test(timeout = 1000)
    public void cancelAfterCompletes() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 10);
        Promise<Boolean> cancel = promise.cancelAfter(true, 5000, TimeUnit.MILLISECONDS);

        assertResolves(false, cancel);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void cancelAfterAlreadyCompleted() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        Promise<Boolean> cancel = promise.cancelAfter(true, 5000, TimeUnit.MILLISECONDS);

        assertResolves(false, cancel);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void get() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 10);
        String result = promise.get();
        assertEquals(SUCCESS1, result);
    }

    @Test
    public void getNow() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);
        String result = promise.getNow(SUCCESS2);
        assertEquals(SUCCESS1, result);
    }

    @Test
    public void getNowDefaultValue() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        String result = promise.getNow(SUCCESS1);
        assertEquals(SUCCESS1, result);
    }

    @Test(expected = ExecutionException.class)
    public void getNowRejected() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise = Promise.rejected(exception);
        String ignored = promise.getNow(SUCCESS1);
    }

    @Test(expected = CancellationException.class)
    public void getNowCancelled() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();
        promise.cancel(true);
        String ignored = promise.getNow(SUCCESS1);
    }

    @Test
    public void getNowInterrupted() throws Throwable {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = promise1.then(PromiseExecutors.CURRENT_THREAD, new OnResolved<String>() {
            @Override
            public void resolved(String result) throws Throwable {
                throw new InterruptedException();
            }
        });

        try {
            String ignored = promise2.getNow(SUCCESS2);
            throw new AssertionFailedError("promise.getNow() should not return successfully");
        }
        catch (ExecutionException exception) {
            assertTrue(exception.getCause() instanceof InterruptedException);
        }
    }

    @Test(expected = ExecutionException.class)
    public void getRejected() throws Throwable {
        Promise<String> promise = rejectAfter(new ArithmeticException(), 10);
        String ignored = promise.get();
        throw new AssertionFailedError("promise.get() should not return successfully");
    }

    @Test(expected = InterruptedException.class)
    public void getInterrupted() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();

        final Thread current = Thread.currentThread();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                current.interrupt();
            }
        });
        thread.setDaemon(true);
        thread.start();

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

    @Test(expected = InterruptedException.class)
    public void getTimedInterrupted() throws Throwable {
        Deferred<String> deferred = Promise.defer();
        Promise<String> promise = deferred.promise();

        final Thread current = Thread.currentThread();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                current.interrupt();
            }
        });
        thread.setDaemon(true);
        thread.start();

        String ignored = promise.get(100, TimeUnit.MILLISECONDS);
        throw new AssertionFailedError("promise.get() should not return successfully");
    }
}
