package org.jpromise;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PromiseCompositionTest {
    public static final String SUCCESS1 = "SUCCESS1";
    public static final String SUCCESS2 = "SUCCESS2";
    public static final String SUCCESS3 = "SUCCESS3";

    @Before
    public void setup() {
        PromiseComposition.clear();
    }

    @Test
    public void register() {
        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        assertTrue(PromiseComposition.register(listener));
    }

    @Test
    public void registerAlreadyRegistered() {
        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        assertTrue(PromiseComposition.register(listener));
        assertFalse(PromiseComposition.register(listener));
    }

    @Test
    public void invokeOne() throws Exception {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = Promises.resolved(SUCCESS2);

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseContinuationListener callback = mock(PromiseContinuationListener.class);
        PromiseContinuationCompletion completion = mock(PromiseContinuationCompletion.class);
        when(listener.composingContinuation(promise1, promise2)).thenReturn(callback);
        when(callback.invokingContinuation(promise1, promise2, SUCCESS1, null)).thenReturn(completion);

        PromiseComposition.register(listener);

        PromiseContinuationListener composedCallback = PromiseComposition.LISTENER.composingContinuation(promise1, promise2);
        verify(listener, times(1)).composingContinuation(promise1, promise2);
        PromiseContinuationCompletion composite = composedCallback.invokingContinuation(promise1, promise2, SUCCESS1, null);
        verify(callback, times(1)).invokingContinuation(promise1, promise2, SUCCESS1, null);
        composite.completed(promise1, promise2, SUCCESS1, null);
        verify(completion, times(1)).completed(promise1, promise2, SUCCESS1, null);
    }

    @Test
    public void invokeTwo() throws Exception {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = Promises.resolved(SUCCESS2);

        PromiseCompositionListener listener1 = mock(PromiseCompositionListener.class);
        PromiseCompositionListener listener2 = mock(PromiseCompositionListener.class);
        PromiseContinuationListener callback = mock(PromiseContinuationListener.class);
        PromiseContinuationCompletion completion = mock(PromiseContinuationCompletion.class);
        when(listener1.composingContinuation(promise1, promise2)).thenReturn(callback);
        when(listener2.composingContinuation(promise1, promise2)).thenReturn(callback);
        when(callback.invokingContinuation(promise1, promise2, SUCCESS1, null)).thenReturn(completion);

        PromiseComposition.register(listener1);
        PromiseComposition.register(listener2);

        PromiseContinuationListener composedCallback = PromiseComposition.LISTENER.composingContinuation(promise1, promise2);
        verify(listener1, times(1)).composingContinuation(promise1, promise2);
        verify(listener2, times(1)).composingContinuation(promise1, promise2);
        PromiseContinuationCompletion composite = composedCallback.invokingContinuation(promise1, promise2, SUCCESS1, null);
        verify(callback, times(2)).invokingContinuation(promise1, promise2, SUCCESS1, null);
        composite.completed(promise1, promise2, SUCCESS1, null);
        verify(completion, times(2)).completed(promise1, promise2, SUCCESS1, null);
    }

    @Test
    public void composingThrows() throws Exception {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = Promises.resolved(SUCCESS2);
        Throwable exception = new RuntimeException();

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseContinuationListener callback = mock(PromiseContinuationListener.class);
        PromiseContinuationCompletion closeable = mock(PromiseContinuationCompletion.class);
        when(listener.composingContinuation(promise1, promise2)).thenThrow(exception);

        PromiseComposition.register(listener);

        PromiseContinuationListener composedCallback = PromiseComposition.LISTENER.composingContinuation(promise1, promise2);
        verify(listener, times(1)).composingContinuation(promise1, promise2);
        PromiseContinuationCompletion composite = composedCallback.invokingContinuation(promise1, promise2, SUCCESS1, null);
        verify(callback, never()).invokingContinuation(promise1, promise2, SUCCESS1, null);
        composite.completed(promise1, promise2, SUCCESS1, null);
        verify(closeable, never()).completed(promise1, promise2, SUCCESS1, null);
    }

    @Test
    public void callbackThrows() throws Exception {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = Promises.resolved(SUCCESS2);
        Throwable exception = new RuntimeException();

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseContinuationListener callback = mock(PromiseContinuationListener.class);
        PromiseContinuationCompletion closeable = mock(PromiseContinuationCompletion.class);
        when(listener.composingContinuation(promise1, promise2)).thenReturn(callback);
        when(callback.invokingContinuation(promise1, promise2, SUCCESS1, null)).thenThrow(exception);

        PromiseComposition.register(listener);

        PromiseContinuationListener composedCallback = PromiseComposition.LISTENER.composingContinuation(promise1, promise2);
        verify(listener, times(1)).composingContinuation(promise1, promise2);
        PromiseContinuationCompletion composite = composedCallback.invokingContinuation(promise1, promise2, SUCCESS1, null);
        verify(callback, times(1)).invokingContinuation(promise1, promise2, SUCCESS1, null);
        composite.completed(promise1, promise2, SUCCESS1, null);
        verify(closeable, never()).completed(promise1, promise2, SUCCESS1, null);
    }

    @Test
    public void completionException() throws Exception {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = Promises.resolved(SUCCESS2);
        Throwable exception = new RuntimeException();

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseContinuationListener callback = mock(PromiseContinuationListener.class);
        PromiseContinuationCompletion completion = mock(PromiseContinuationCompletion.class);
        when(listener.composingContinuation(promise1, promise2)).thenReturn(callback);
        when(callback.invokingContinuation(promise1, promise2, SUCCESS1, null)).thenReturn(completion);

        PromiseComposition.register(listener);

        PromiseContinuationListener composedCallback = PromiseComposition.LISTENER.composingContinuation(promise1, promise2);
        verify(listener, times(1)).composingContinuation(promise1, promise2);
        PromiseContinuationCompletion composite = composedCallback.invokingContinuation(promise1, promise2, SUCCESS1, null);
        verify(callback, times(1)).invokingContinuation(promise1, promise2, SUCCESS1, null);
        verify(completion, never()).completed(promise1, promise2, SUCCESS1, null);
    }

    @Test
    public void completionThrows() throws Exception {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = Promises.resolved(SUCCESS2);
        Throwable exception = new RuntimeException();

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseContinuationListener callback = mock(PromiseContinuationListener.class);
        PromiseContinuationCompletion completion = mock(PromiseContinuationCompletion.class);
        when(listener.composingContinuation(promise1, promise2)).thenReturn(callback);
        when(callback.invokingContinuation(promise1, promise2, SUCCESS1, null)).thenReturn(completion);
        doThrow(exception).when(completion).completed(promise1, promise2, SUCCESS1, null);

        PromiseComposition.register(listener);

        PromiseContinuationListener composedCallback = PromiseComposition.LISTENER.composingContinuation(promise1, promise2);
        verify(listener, times(1)).composingContinuation(promise1, promise2);
        PromiseContinuationCompletion composite = composedCallback.invokingContinuation(promise1, promise2, SUCCESS1, null);
        verify(callback, times(1)).invokingContinuation(promise1, promise2, SUCCESS1, null);
        composite.completed(promise1, promise2, SUCCESS1, null);
        verify(completion, times(1)).completed(promise1, promise2, SUCCESS1, null);
    }
}
