package org.jpromise;

import org.junit.Test;

import java.util.concurrent.Callable;
import static org.jpromise.PromiseHelpers.assertResolves;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultPromiseServiceTest {
    private static final String SUCCESS1 = "SUCCESS1";

    @Test
    public void submitWithRunnable() throws Throwable {
        Runnable runnable = mock(Runnable.class);
        PromiseService service = new DefaultPromiseService();
        Promise<Void> promise = service.submit(runnable);
        assertResolves(promise);
        verify(runnable, times(1)).run();
    }

    @Test
    public void submitWithCallable() throws Throwable {
        @SuppressWarnings("unchecked")
        Callable<String> callable = mock(Callable.class);
        when(callable.call()).thenReturn(SUCCESS1);
        PromiseService service = new DefaultPromiseService();
        Promise<String> promise = service.submit(callable);
        assertResolves(SUCCESS1, promise);
        verify(callable, times(1)).call();
    }

    @Test
    public void submitWithRunnableAndValue() throws Throwable {
        Runnable runnable = mock(Runnable.class);
        PromiseService service = new DefaultPromiseService();
        Promise<String> promise = service.submit(runnable, SUCCESS1);
        assertResolves(SUCCESS1, promise);
        verify(runnable, times(1)).run();
    }
}
