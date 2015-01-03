package org.jpromise;

import org.jpromise.functions.OnFulfilled;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.Executor;

import static org.jpromise.PromiseHelpers.assertFulfills;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PromiseExecutorsTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final Executor EXECUTOR = mock(Executor.class);

    @After
    public void teardown() {
        reset(EXECUTOR);
        PromiseExecutors.setContextExecutor(null);
    }

    @Test
    public void setContextExecutor() {
        PromiseExecutors.setContextExecutor(EXECUTOR);

        Executor other = PromiseExecutors.getContextExecutor();

        assertEquals(EXECUTOR, other);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void useContextExecutorExplicitly() throws Throwable {
        OnFulfilled<String> callback = mock(OnFulfilled.class);
        PromiseExecutors.setContextExecutor(EXECUTOR);

        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = promise1.then(PromiseExecutors.getContextExecutor(), callback);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(EXECUTOR, times(1)).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();

        assertFulfills(SUCCESS1, promise2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void useContextExecutorImplicitly() throws Throwable {
        OnFulfilled<String> callback = mock(OnFulfilled.class);
        PromiseExecutors.setContextExecutor(EXECUTOR);

        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = promise1.then(callback);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(EXECUTOR, times(1)).execute(captor.capture());
        Runnable runnable = captor.getValue();
        runnable.run();

        assertFulfills(SUCCESS1, promise2);
    }
}
