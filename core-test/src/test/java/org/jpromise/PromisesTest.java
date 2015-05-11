package org.jpromise;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.*;

import static org.jpromise.PromiseHelpers.assertRejects;
import static org.jpromise.PromiseHelpers.fulfillAfter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class PromisesTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String FAIL1 = "FAIL1";

    @Test(expected = IllegalStateException.class)
    public void PromisesCannotBeCreated() throws Throwable {
        Class<Promises> promisesClass = Promises.class;
        Constructor<?>[] constructors = promisesClass.getDeclaredConstructors();
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
    public void defer() throws Throwable {
        Deferred<String> deferred = Promises.defer();
        Promise<String> promise = deferred.promise();
        assertFalse(promise.isDone());
        assertTrue(promise.isPending());
        assertEquals(PromiseState.PENDING, promise.state());
    }

    @Test
    public void deferWithType() throws Throwable {
        Deferred<String> deferred = Promises.defer(String.class);
        Promise<String> promise = deferred.promise();
        assertFalse(promise.isDone());
        assertTrue(promise.isPending());
        assertEquals(PromiseState.PENDING, promise.state());
    }

    @Test
    public void pending() {
        Deferred<String> deferred = Promises.defer();
        Promise<String> promise = deferred.promise();
        assertEquals(PromiseState.PENDING, promise.state());
        assertFalse(promise.isFulfilled());
        assertFalse(promise.isRejected());
        assertFalse(promise.isDone());
        assertFalse(promise.isCancelled());
        assertTrue(promise.isPending());
        assertEquals(promise.toString(), "[PENDING]");
    }

    @Test
    public void fulfilled() throws Throwable {
        Promise<String> promise = Promises.fulfilled(SUCCESS1);

        PromiseHelpers.assertFulfills(SUCCESS1, promise);
        assertEquals("[FULFILLED]: SUCCESS1", promise.toString());
    }

    @Test
    public void fulfilledVoid() throws Throwable {
        Promise<Void> promise = Promises.fulfilled();
        PromiseHelpers.assertFulfills(promise);
    }

    @Test
    public void rejected() throws Throwable {
        Exception exception = new Exception(FAIL1);
        Promise<String> promise = Promises.rejected(exception);

        assertRejects(exception, promise);
        assertEquals("[REJECTED]: " + exception.toString(), promise.toString());
    }

    @Test
    public void typedRejected() throws Throwable {
        Exception exception = new Exception(FAIL1);
        Promise<String> promise = Promises.rejected(String.class, exception);

        assertRejects(exception, promise);
        assertEquals("[REJECTED]: " + exception.toString(), promise.toString());
    }

    @Test
    public void fromFutureDone() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(true);
        when(future.get()).thenReturn(SUCCESS1);

        Promise<String> promise = Promises.fromFuture(future);

        PromiseHelpers.assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void fromFutureWithPromiseReturnsSelf() throws Throwable {
        Future<String> future = Promises.fulfilled(SUCCESS1);

        Promise<String> promise = Promises.fromFuture(future);

        assertEquals(future, promise);
        PromiseHelpers.assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void fromFutureWithPromiseAndTimeOutEnforcesTimeout() throws Throwable {
        Future<String> future = fulfillAfter(SUCCESS1, 1000);

        Promise<String> promise = Promises.fromFuture(future, 10, TimeUnit.MILLISECONDS);

        assertRejects(CancellationException.class, promise);
    }

    @Test
    public void fromFutureCompletesEventually() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        when(future.get()).thenReturn(SUCCESS1);

        Promise<String> promise = Promises.fromFuture(future);

        PromiseHelpers.assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void fromFutureThrows() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        Exception exception = new Exception();
        when(future.get()).thenThrow(new ExecutionException(exception));

        Promise<String> promise = Promises.fromFuture(future);

        assertRejects(exception, promise);
    }

    @Test
    public void fromFutureThrowsWithoutCause() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        ExecutionException exception = new ExecutionException(null);
        when(future.get()).thenThrow(exception);

        Promise<String> promise = Promises.fromFuture(future);

        assertRejects(exception, promise);
    }

    @Test
    public void fromFutureThrowsEventually() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        Exception exception = new Exception();
        when(future.get()).thenThrow(new ExecutionException(exception));

        Promise<String> promise = Promises.fromFuture(future);

        assertRejects(exception, promise);
    }

    @Test
    public void fromFutureTimesOut() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        TimeoutException exception = new TimeoutException();
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(exception);

        Promise<String> promise = Promises.fromFuture(future, 10, TimeUnit.MILLISECONDS);

        assertRejects(exception, promise);
    }

    @Test
    public void fromFutureCancelled() throws Throwable {
        @SuppressWarnings("unchecked")
        Future<String> future = mock(Future.class);
        when(future.isDone()).thenReturn(false);
        when(future.cancel(anyBoolean())).thenReturn(true);
        when(future.get()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return SUCCESS1;
            }
        });

        Promise<String> promise = Promises.fromFuture(future);
        assertTrue(promise.cancel(true));

        assertRejects(CancellationException.class, promise);
        verify(future).cancel(true);
    }

    @Test
    public void createWithRunnable() throws Throwable {
        Runnable runnable = mock(Runnable.class);
        Promise<Void> promise = Promises.create(runnable);
        PromiseHelpers.assertFulfills(promise);
        verify(runnable, times(1)).run();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createWithCallable() throws Throwable {
        Callable<String> callable = mock(Callable.class);
        when(callable.call()).thenReturn(SUCCESS1);
        Promise<String> promise = Promises.create(callable);
        PromiseHelpers.assertFulfills(SUCCESS1, promise);
        verify(callable, times(1)).call();
    }

    @Test
    public void createWithRunnableAndValue() throws Throwable {
        Runnable runnable = mock(Runnable.class);
        Promise<String> promise = Promises.create(runnable, SUCCESS1);
        PromiseHelpers.assertFulfills(SUCCESS1, promise);
        verify(runnable, times(1)).run();
    }

    @Test
    public void createWithExecutorAndRunnable() throws Throwable {
        Runnable task = mock(Runnable.class);
        Executor executor = mock(Executor.class);
        doNothing().when(task).run();
        doNothing().when(executor).execute(any(Runnable.class));

        Promise<Void> promise = Promises.create(executor, task);

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();
        verify(task, times(1)).run();

        PromiseHelpers.assertFulfills(promise);
    }

    @Test
    public void createWithExecutorAndRunnableAndValue() throws Throwable {
        Runnable task = mock(Runnable.class);
        Executor executor = mock(Executor.class);
        doNothing().when(task).run();
        doNothing().when(executor).execute(any(Runnable.class));

        Promise<String> promise = Promises.create(executor, task, SUCCESS1);

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();
        verify(task, times(1)).run();

        PromiseHelpers.assertFulfills(SUCCESS1, promise);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createWithExecutorAndCallable() throws Throwable {
        Callable<String> task = mock(Callable.class);
        Executor executor = mock(Executor.class);
        when(task.call()).thenReturn(SUCCESS1);
        doNothing().when(executor).execute(any(Runnable.class));

        Promise<String> promise = Promises.create(executor, task);

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();
        verify(task, times(1)).call();

        PromiseHelpers.assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void createWithExecutorAndRunnableThrows() throws Throwable {
        final RuntimeException exception = new RuntimeException();
        Runnable task = mock(Runnable.class);
        Executor executor = mock(Executor.class);
        doThrow(exception).when(task).run();
        doNothing().when(executor).execute(any(Runnable.class));

        Promise<Void> promise = Promises.create(executor, task);

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();
        verify(task, times(1)).run();

        assertRejects(exception, promise);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createWithExecutorAndCallableThrows() throws Throwable {
        final RuntimeException exception = new RuntimeException();
        Callable<String> task = mock(Callable.class);
        Executor executor = mock(Executor.class);
        when(task.call()).thenThrow(exception);
        doNothing().when(executor).execute(any(Runnable.class));

        Promise<String> promise = Promises.create(executor, task);

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor, times(1)).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();
        verify(task, times(1)).call();

        assertRejects(exception, promise);
    }
}
