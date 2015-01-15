package org.jpromise;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RunnablePromiseTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final Throwable EXCEPTION = new Exception();

    @Test
    @SuppressWarnings("unchecked")
    public void fulfills() throws Throwable {
        Callable<String> task = mock(Callable.class);
        when(task.call()).thenReturn(SUCCESS1);

        RunnablePromise<String> promise = new RunnablePromise<String>(task);
        assertFalse(promise.isDone());
        promise.run();

        assertFulfills(SUCCESS1, promise);
        verify(task, times(1)).call();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rejects() throws Throwable {
        Callable<String> task = mock(Callable.class);
        when(task.call()).thenThrow(new RuntimeException(EXCEPTION));

        RunnablePromise<String> promise = new RunnablePromise<String>(task);
        assertFalse(promise.isDone());
        promise.run();

        assertRejects(EXCEPTION, promise);
        verify(task, times(1)).call();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cancelBeforeRun() throws Throwable {
        Callable<String> task = mock(Callable.class);
        when(task.call()).thenReturn(SUCCESS1);

        RunnablePromise<String> promise = new RunnablePromise<String>(task);
        assertFalse(promise.isDone());

        assertTrue(promise.cancel(true));
        assertRejects(CancellationException.class, promise);
        verify(task, never()).call();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cancelAfterRun() throws Throwable {
        Callable<String> task = mock(Callable.class);
        when(task.call()).thenReturn(SUCCESS1);

        RunnablePromise<String> promise = new RunnablePromise<String>(task);
        assertFalse(promise.isDone());

        promise.run();
        assertFalse(promise.cancel(true));
        assertFulfills(SUCCESS1, promise);
        verify(task, times(1)).call();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cancelDuringRun() throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
        final Runnable runnable = mock(Runnable.class);
        Callable<String> task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                latch.countDown();
                Thread.sleep(2000);
                runnable.run();
                return SUCCESS1;
            }
        };

        RunnablePromise<String> promise = new RunnablePromise<String>(task);
        assertFalse(promise.isDone());

        Thread thread = new Thread(promise);
        thread.start();
        latch.await();
        assertTrue(promise.cancel(true));
        assertRejects(CancellationException.class, promise);
        thread.join();
        verify(runnable, never()).run();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void alreadyRun() throws Throwable {
        Callable<String> task = mock(Callable.class);
        when(task.call()).thenReturn(SUCCESS1);

        RunnablePromise<String> promise = new RunnablePromise<String>(task);
        assertFalse(promise.isDone());
        promise.run();
        promise.run();

        assertFulfills(SUCCESS1, promise);
        verify(task, times(1)).call();
    }
}
