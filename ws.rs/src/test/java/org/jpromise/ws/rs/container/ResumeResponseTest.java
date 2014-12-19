package org.jpromise.ws.rs.container;

import org.jpromise.Promise;
import org.junit.Test;

import javax.ws.rs.container.AsyncResponse;

import static org.jpromise.PromiseHelpers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ResumeResponseTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final Throwable EXCEPTION = new Throwable();

    @Test
    public void resumeWithResult() throws Throwable {
        AsyncResponse response = mock(AsyncResponse.class);
        when(response.resume(eq(SUCCESS1))).thenReturn(true);

        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);

        Promise<String> promise2 = promise1.whenCompleted(new ResumeResponse<String>(response));

        assertResolves(SUCCESS1, promise2);
        verify(response, times(1)).resume(eq(SUCCESS1));
        verify(response, never()).resume(any(Throwable.class));
    }

    @Test
    public void resumeWithException() throws Throwable {
        AsyncResponse response = mock(AsyncResponse.class);
        when(response.resume(eq(EXCEPTION))).thenReturn(true);

        Promise<String> promise1 = rejectAfter(EXCEPTION, 10);

        Promise<String> promise2 = promise1.whenCompleted(new ResumeResponse<String>(response));

        assertRejects(EXCEPTION, promise2);
        verify(response, times(1)).resume(eq(EXCEPTION));
        verify(response, never()).resume(anyString());
    }
}
