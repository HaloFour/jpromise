package org.jpromise;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import static org.jpromise.PromiseHelpers.assertRejects;
import static org.jpromise.PromiseHelpers.assertResolves;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class ExecutorPromiseServiceTest {
    private static final String SUCCESS1 = "SUCCESS1";

    @Test
    public void submitWithRunnable() throws Throwable {
        Executor executor = mock(Executor.class);
        doNothing().when(executor).execute(any(Runnable.class));

        PromiseService service = new ExecutorPromiseService(executor);
        Promise<Void> promise = service.submit(new Runnable() {
            @Override
            public void run() {

            }
        });

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();

        assertResolves(promise);
    }

    @Test
    public void submitWithCallable() throws Throwable {
        Executor executor = mock(Executor.class);
        doNothing().when(executor).execute(any(Runnable.class));

        PromiseService service = new ExecutorPromiseService(executor);
        Promise<String> promise = service.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return SUCCESS1;
            }
        });

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void submitWithRunnableThrows() throws Throwable {
        Executor executor = mock(Executor.class);
        doNothing().when(executor).execute(any(Runnable.class));

        PromiseService service = new ExecutorPromiseService(executor);
        final RuntimeException exception = new RuntimeException();
        Promise<Void> promise = service.submit(new Runnable() {
            @Override
            public void run() {
                throw exception;
            }
        });

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();

        assertRejects(exception, promise);
    }

    @Test
    public void submitWithCallableThrows() throws Throwable {
        Executor executor = mock(Executor.class);
        doNothing().when(executor).execute(any(Runnable.class));

        final Exception exception = new Exception();
        PromiseService service = new ExecutorPromiseService(executor);
        Promise<String> promise = service.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw exception;
            }
        });

        assertFalse(promise.isDone());

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();

        assertRejects(exception, promise);
    }
}
