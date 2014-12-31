package org.jpromise;

import org.junit.Test;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultPromiseServiceTest {
    private static final String SUCCESS1 = "SUCCESS1";

    @Test
    public void submitWithRunnable() throws Throwable {
        Runnable runnable = mock(Runnable.class);
        Promise<Void> promise = DefaultPromiseService.INSTANCE.submit(runnable);
        PromiseHelpers.assertFulfills(promise);
        verify(runnable, times(1)).run();
    }

    @Test
    public void submitWithCallable() throws Throwable {
        @SuppressWarnings("unchecked")
        Callable<String> callable = mock(Callable.class);
        when(callable.call()).thenReturn(SUCCESS1);
        Promise<String> promise = DefaultPromiseService.INSTANCE.submit(callable);
        PromiseHelpers.assertFulfills(SUCCESS1, promise);
        verify(callable, times(1)).call();
    }

    @Test
    public void submitWithRunnableAndValue() throws Throwable {
        Runnable runnable = mock(Runnable.class);
        Promise<String> promise = DefaultPromiseService.INSTANCE.submit(runnable, SUCCESS1);
        PromiseHelpers.assertFulfills(SUCCESS1, promise);
        verify(runnable, times(1)).run();
    }
}
