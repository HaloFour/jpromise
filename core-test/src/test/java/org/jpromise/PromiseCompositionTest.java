package org.jpromise;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
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
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseCallbackListener callback = mock(PromiseCallbackListener.class);
        AutoCloseable closeable = mock(AutoCloseable.class);
        when(listener.composingCallback(promise1, promise2)).thenReturn(callback);
        when(callback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)).thenReturn(closeable);

        PromiseComposition.register(listener);

        PromiseCallbackListener composedCallback = PromiseComposition.LISTENER.composingCallback(promise1, promise2);
        verify(listener, times(1)).composingCallback(promise1, promise2);
        try (AutoCloseable ignored = composedCallback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)) {
            verify(callback, times(1)).invokingPromiseCallback(promise1, promise2, SUCCESS1, null);
        }
        verify(closeable, times(1)).close();
    }

    @Test
    public void invokeTwo() throws Exception {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);

        PromiseCompositionListener listener1 = mock(PromiseCompositionListener.class);
        PromiseCompositionListener listener2 = mock(PromiseCompositionListener.class);
        PromiseCallbackListener callback = mock(PromiseCallbackListener.class);
        AutoCloseable closeable = mock(AutoCloseable.class);
        when(listener1.composingCallback(promise1, promise2)).thenReturn(callback);
        when(listener2.composingCallback(promise1, promise2)).thenReturn(callback);
        when(callback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)).thenReturn(closeable);

        PromiseComposition.register(listener1);
        PromiseComposition.register(listener2);

        PromiseCallbackListener composedCallback = PromiseComposition.LISTENER.composingCallback(promise1, promise2);
        verify(listener1, times(1)).composingCallback(promise1, promise2);
        verify(listener2, times(1)).composingCallback(promise1, promise2);
        try (AutoCloseable ignored = composedCallback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)) {
            verify(callback, times(2)).invokingPromiseCallback(promise1, promise2, SUCCESS1, null);
        }
        verify(closeable, times(2)).close();
    }

    @Test
    public void composingThrows() throws Exception {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);
        Throwable exception = new RuntimeException();

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseCallbackListener callback = mock(PromiseCallbackListener.class);
        AutoCloseable closeable = mock(AutoCloseable.class);
        when(listener.composingCallback(promise1, promise2)).thenThrow(exception);

        PromiseComposition.register(listener);

        PromiseCallbackListener composedCallback = PromiseComposition.LISTENER.composingCallback(promise1, promise2);
        verify(listener, times(1)).composingCallback(promise1, promise2);
        try (AutoCloseable ignored = composedCallback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)) {
            verify(callback, never()).invokingPromiseCallback(promise1, promise2, SUCCESS1, null);
        }
        verify(listener, times(1)).exception(exception);
        verify(closeable, never()).close();
    }

    @Test
    public void callbackThrows() throws Exception {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);
        Throwable exception = new RuntimeException();

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseCallbackListener callback = mock(PromiseCallbackListener.class);
        AutoCloseable closeable = mock(AutoCloseable.class);
        when(listener.composingCallback(promise1, promise2)).thenReturn(callback);
        when(callback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)).thenThrow(exception);

        PromiseComposition.register(listener);

        PromiseCallbackListener composedCallback = PromiseComposition.LISTENER.composingCallback(promise1, promise2);
        verify(listener, times(1)).composingCallback(promise1, promise2);
        try (AutoCloseable ignored = composedCallback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)) {
            verify(callback, times(1)).invokingPromiseCallback(promise1, promise2, SUCCESS1, null);
        }
        verify(listener, times(1)).exception(exception);
        verify(closeable, never()).close();
    }

    @Test
    public void closeableThrows() throws Exception {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);
        Throwable exception = new Exception();

        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        PromiseCallbackListener callback = mock(PromiseCallbackListener.class);
        AutoCloseable closeable = mock(AutoCloseable.class);
        when(listener.composingCallback(promise1, promise2)).thenReturn(callback);
        when(callback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)).thenReturn(closeable);
        doThrow(exception).when(closeable).close();

        PromiseComposition.register(listener);

        PromiseCallbackListener composedCallback = PromiseComposition.LISTENER.composingCallback(promise1, promise2);
        verify(listener, times(1)).composingCallback(promise1, promise2);
        try (AutoCloseable ignored = composedCallback.invokingPromiseCallback(promise1, promise2, SUCCESS1, null)) {
            verify(callback, times(1)).invokingPromiseCallback(promise1, promise2, SUCCESS1, null);
        }
        verify(closeable, times(1)).close();
        verify(listener, times(1)).exception(exception);
    }

    @Test
    public void compositePropagatesException() {
        PromiseCompositionListener listener = mock(PromiseCompositionListener.class);
        assertTrue(PromiseComposition.register(listener));
        Throwable exception = new Throwable();

        PromiseComposition.LISTENER.exception(exception);

        verify(listener, times(1)).exception(exception);
    }
}
