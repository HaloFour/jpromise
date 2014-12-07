package org.jpromise;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static org.jpromise.PromiseHelpers.assertRejects;
import static org.jpromise.PromiseHelpers.assertResolves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PromiseManagerTest {
    private static final String SUCCESS1 = "SUCCESS1";

    @Test
    public void create() throws Throwable {
        Executor executor = mock(Executor.class);
        doNothing().when(executor).execute(any(Runnable.class));

        Promise<String> promise = PromiseManager.create(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return SUCCESS1;
            }
        }, executor);

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
        Promise<String> promise = PromiseManager.create(new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw exception;
            }
        }, executor);

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
}
