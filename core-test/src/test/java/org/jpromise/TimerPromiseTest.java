package org.jpromise;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TimerPromiseTest {
    @Test
    public void fulfills() throws Throwable {
        TimerPromise promise = new TimerPromise(10, TimeUnit.MILLISECONDS);
        assertFulfills(promise);
    }

    @Test
    public void cancel() throws Throwable {
        Timer timer = mock(Timer.class);
        TimerPromise promise = new TimerPromise(timer, 10, TimeUnit.MILLISECONDS);
        assertTrue(promise.cancel(true));
        verify(timer, times(1)).cancel();
    }

    @Test
    public void cancelFulfilled() throws Throwable {
        Timer timer = mock(Timer.class);
        TimerPromise promise = new TimerPromise(timer, 10, TimeUnit.MILLISECONDS);

        ArgumentCaptor<TimerTask> captor = ArgumentCaptor.forClass(TimerTask.class);
        verify(timer, times(1)).schedule(captor.capture(), eq(10L));
        TimerTask task = captor.getValue();

        task.run();

        assertFalse(promise.cancel(true));
        verify(timer, times(0)).cancel();
    }
}
