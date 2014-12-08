package org.jpromise;

import org.jpromise.functions.OnResolved;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PromiseManagerTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String SUCCESS3 = "SUCCESS3";
    private static final String SUCCESS4 = "SUCCESS4";

    @Test
    public void create() throws Throwable {
        Executor executor = mock(Executor.class);
        doNothing().when(executor).execute(any(Runnable.class));

        Promise<String> promise = PromiseManager.create(executor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return SUCCESS1;
            }
        });

        assertFalse(promise.isDone());

        verify(executor).execute(argThat(new ArgumentMatcher<Runnable>() {
            @Override
            public boolean matches(Object argument) {
                if (argument instanceof Runnable) {
                    ((Runnable)argument).run();
                    return true;
                }
                return false;
            }
        }));

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void createRejects() throws Throwable {
        Executor executor = mock(Executor.class);
        doNothing().when(executor).execute(any(Runnable.class));

        final Exception exception = new Exception();
        Promise<String> promise = PromiseManager.create(executor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw exception;
            }
        });

        assertFalse(promise.isDone());

        verify(executor).execute(argThat(new ArgumentMatcher<Runnable>() {
            @Override
            public boolean matches(Object argument) {
                if (argument instanceof Runnable) {
                    ((Runnable)argument).run();
                    return true;
                }
                return false;
            }
        }));

        assertRejects(exception, promise);
    }

    @Test
    public void fromFutureDone() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(true);
        when(future.get()).thenReturn(SUCCESS1);

        Promise<String> promise = PromiseManager.fromFuture(future);

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void fromFutureWait() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        when(future.get()).thenReturn(SUCCESS1);

        Promise<String> promise = PromiseManager.fromFuture(future);

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void fromFutureThrows() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        Exception exception = new Exception();
        when(future.get()).thenThrow(new ExecutionException(exception));

        Promise<String> promise = PromiseManager.fromFuture(future);

        assertRejects(exception, promise);
    }

    @Test
    public void fromFutureTimesOut() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        TimeoutException exception = new TimeoutException();
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(exception);

        Promise<String> promise = PromiseManager.fromFuture(future, 10, TimeUnit.MILLISECONDS);

        assertRejects(exception, promise);
    }

    @Test
    public void whenAllComplete() throws Throwable {
        final Promise<String> promise1 = Promise.resolved(SUCCESS1);
        final Promise<String> promise2 = Promise.resolved(SUCCESS2);
        final Promise<String> promise3 = Promise.resolved(SUCCESS3);
        final Promise<String> promise4 = Promise.resolved(SUCCESS4);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteEventually() throws Throwable {
        final Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        final Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        final Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        final Promise<String> promise4 = resolveAfter(SUCCESS4, 10);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteRejected() throws Throwable {
        final Throwable exception = new Throwable();
        final Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        final Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        final Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        final Promise<String> promise4 = Promise.rejected(exception);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertRejects(exception, promise4);

                    }
                });

        assertResolves(promise);
    }


    @Test
    public void whenAllCompleteEmptyList() throws Throwable {
        List<Promise<String>> list = new ArrayList<>(0);
        Promise<Void> promise = PromiseManager.whenAllCompleted(list);

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<Void> promise = PromiseManager.whenAllCompleted(list);

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteListContainsNull() throws Throwable {
        final Promise<String> promise1 = Promise.resolved(SUCCESS1);
        final Promise<String> promise2 = Promise.resolved(SUCCESS2);
        final Promise<String> promise3 = Promise.resolved(SUCCESS3);
        final Promise<String> promise4 = null;

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertNull(promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllResolved() throws Throwable {
        final Promise<String> promise1 = Promise.resolved(SUCCESS1);
        final Promise<String> promise2 = Promise.resolved(SUCCESS2);
        final Promise<String> promise3 = Promise.resolved(SUCCESS3);
        final Promise<String> promise4 = Promise.resolved(SUCCESS4);

        Promise<Void> promise = PromiseManager.whenAllResolved(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedEventually() throws Throwable {
        final Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        final Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        final Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        final Promise<String> promise4 = resolveAfter(SUCCESS4, 10);

        Promise<Void> promise = PromiseManager.whenAllResolved(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedRejected() throws Throwable {
        final Throwable exception = new Throwable();
        final Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        final Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        final Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        final Promise<String> promise4 = Promise.rejected(exception);

        Promise<Void> promise = PromiseManager.whenAllResolved(promise1, promise2, promise3, promise4);

        assertRejects(exception, promise);
    }


    @Test
    public void whenAllResolvedEmptyList() throws Throwable {
        List<Promise<String>> list = new ArrayList<>(0);
        Promise<Void> promise = PromiseManager.whenAllResolved(list);

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<Void> promise = PromiseManager.whenAllResolved(list);

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedListContainsNull() throws Throwable {
        final Promise<String> promise1 = Promise.resolved(SUCCESS1);
        final Promise<String> promise2 = Promise.resolved(SUCCESS2);
        final Promise<String> promise3 = Promise.resolved(SUCCESS3);
        final Promise<String> promise4 = null;

        Promise<Void> promise = PromiseManager.whenAllResolved(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertNull(promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAnyComplete() throws Throwable {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyComplete(promise1, promise2);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyCompleteEventually() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyComplete(promise1, promise2);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyCompleteRejects() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promise.rejected(exception);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyComplete(promise1, promise2);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS2, promise2);
        assertRejects(exception, promise);
    }

    @Test
    public void whenAnyCompleteRejectedAfterResolved() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = rejectAfter(exception, 100);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);

        Promise<String> promise = PromiseManager.whenAnyComplete(promise1, promise2);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS2, promise);
    }

    @Test
    public void whenAnyCompleteNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<String> promise = PromiseManager.whenAnyComplete(list);

        assertFalse(promise.isDone());
    }

    @Test
    public void whenAnyCompleteListContainsNulls() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = null;

        Promise<String> promise = PromiseManager.whenAnyComplete(promise1, promise2, promise3);

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolved() throws Throwable {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolvedEventually() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolvedRejects() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promise.rejected(exception);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS2, promise);
    }

    @Test
    public void whenAnyResolvedRejectedAfterResolved() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = rejectAfter(exception, 100);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS2, promise);
    }

    @Test
    public void whenAnyResolvedNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<String> promise = PromiseManager.whenAnyResolved(list);

        assertFalse(promise.isDone());
    }

    @Test
    public void whenAnyResolvedListContainsNulls() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = null;

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2, promise3);

        assertResolves(SUCCESS1, promise);
    }
}
