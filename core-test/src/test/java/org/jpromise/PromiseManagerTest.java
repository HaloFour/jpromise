package org.jpromise;

import org.jpromise.functions.OnResolved;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PromiseManagerTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String SUCCESS3 = "SUCCESS3";
    private static final String SUCCESS4 = "SUCCESS4";
    private static final String SUCCESS5 = "SUCCESS5";

    @Test(expected = IllegalStateException.class)
    public void PromiseManagerCannotBeCreated() throws Throwable {
        Class<PromiseManager> promiseManagerClass = PromiseManager.class;
        Constructor<?>[] constructors = promiseManagerClass.getDeclaredConstructors();
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
    public void whenAllComplete() throws Throwable {
        final Promise<String> promise1 = Promises.resolved(SUCCESS1);
        final Promise<String> promise2 = Promises.resolved(SUCCESS2);
        final Promise<String> promise3 = Promises.resolved(SUCCESS3);
        final Promise<String> promise4 = Promises.resolved(SUCCESS4);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteEventually() throws Throwable {
        final Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        final Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        final Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        final Promise<String> promise4 = resolveAfter(SUCCESS4, 10);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteRejected() throws Throwable {
        final Throwable exception = new Throwable();
        final Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        final Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        final Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        final Promise<String> promise4 = Promises.rejected(exception);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertRejects(exception, promise4);

                    }
                });

        assertResolves(promise);
    }


    @Test
    public void whenAllCompleteEmptyList() throws Throwable {
        List<Promise<String>> list = new ArrayList<Promise<String>>(0);
        Promise<Void> promise = PromiseManager.whenAllCompleted(list);

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<Void> promise = PromiseManager.whenAllCompleted(list);

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteNullArray() throws Throwable {
        Promise<String>[] array = null;
        Promise<Void> promise = PromiseManager.whenAllCompleted(array);

        assertResolves(promise);
    }

    @Test
    public void whenAllCompleteListContainsNull() throws Throwable {
        final Promise<String> promise1 = Promises.resolved(SUCCESS1);
        final Promise<String> promise2 = Promises.resolved(SUCCESS2);
        final Promise<String> promise3 = Promises.resolved(SUCCESS3);
        final Promise<String> promise4 = null;

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertNull(promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllResolved() throws Throwable {
        final Promise<String> promise1 = Promises.resolved(SUCCESS1);
        final Promise<String> promise2 = Promises.resolved(SUCCESS2);
        final Promise<String> promise3 = Promises.resolved(SUCCESS3);
        final Promise<String> promise4 = Promises.resolved(SUCCESS4);

        Promise<Void> promise = PromiseManager.whenAllResolved(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedWithCallback() throws Throwable {
        @SuppressWarnings("unchecked")
        OnResolved<String> callback = (OnResolved<String>)mock(OnResolved.class);

        final Promise<String> promise1 = Promises.resolved(SUCCESS1);
        final Promise<String> promise2 = Promises.resolved(SUCCESS2);
        final Promise<String> promise3 = Promises.resolved(SUCCESS3);
        final Promise<String> promise4 = Promises.resolved(SUCCESS4);

        List<Promise<String>> promises = new ArrayList<Promise<String>>(4);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);

        Promise<Void> promise = PromiseManager.whenAllResolved(promises, callback)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
        verify(callback, times(4)).resolved(anyString());
        verify(callback, times(1)).resolved(SUCCESS1);
        verify(callback, times(1)).resolved(SUCCESS2);
        verify(callback, times(1)).resolved(SUCCESS3);
        verify(callback, times(1)).resolved(SUCCESS4);
    }

    @Test
    public void whenAllResolvedEventually() throws Throwable {
        final Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        final Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        final Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        final Promise<String> promise4 = resolveAfter(SUCCESS4, 10);

        Promise<Void> promise = PromiseManager.whenAllResolved(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertResolves(SUCCESS4, promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedRejected() throws Throwable {
        final Throwable exception = new Throwable();
        final Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        final Promise<String> promise2 = resolveAfter(SUCCESS2, 10);
        final Promise<String> promise3 = resolveAfter(SUCCESS3, 10);
        final Promise<String> promise4 = Promises.rejected(exception);

        Promise<Void> promise = PromiseManager.whenAllResolved(promise1, promise2, promise3, promise4);

        assertRejects(exception, promise);
    }


    @Test
    public void whenAllResolvedEmptyList() throws Throwable {
        List<Promise<String>> list = new ArrayList<Promise<String>>(0);
        Promise<Void> promise = PromiseManager.whenAllResolved(list);

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<Void> promise = PromiseManager.whenAllResolved(list);

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedNullArray() throws Throwable {
        Promise<String>[] array = null;
        Promise<Void> promise = PromiseManager.whenAllResolved(array);

        assertResolves(promise);
    }

    @Test
    public void whenAllResolvedListContainsNull() throws Throwable {
        final Promise<String> promise1 = Promises.resolved(SUCCESS1);
        final Promise<String> promise2 = Promises.resolved(SUCCESS2);
        final Promise<String> promise3 = Promises.resolved(SUCCESS3);
        final Promise<String> promise4 = null;

        Promise<Void> promise = PromiseManager.whenAllResolved(promise1, promise2, promise3, promise4)
                .then(new OnResolved<Void>() {
                    @Override
                    public void resolved(Void result) throws Throwable {
                        assertResolves(SUCCESS1, promise1);
                        assertResolves(SUCCESS2, promise2);
                        assertResolves(SUCCESS3, promise3);
                        assertNull(promise4);
                    }
                });

        assertResolves(promise);
    }

    @Test
    public void whenAnyComplete2() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyComplete3() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2, promise3);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS3, promise3);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyComplete4() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 100);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2, promise3, promise4);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS3, promise3);
        assertResolves(SUCCESS4, promise4);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyComplete5() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 100);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 100);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2, promise3, promise4, promise5);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS3, promise3);
        assertResolves(SUCCESS4, promise4);
        assertResolves(SUCCESS5, promise5);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenAnyCompleteArray() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 100);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 100);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 100);

        Promise<String>[] array = (Promise<String>[])new Promise [] {
                promise1, promise2, promise3, promise4, promise5
        };

        Promise<String> promise = PromiseManager.whenAnyCompleted(array);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS3, promise3);
        assertResolves(SUCCESS4, promise4);
        assertResolves(SUCCESS5, promise5);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyCompleteEventually() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyCompleteRejects() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promises.rejected(exception);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS2, promise2);
        assertRejects(exception, promise);
    }

    @Test
    public void whenAnyCompleteRejectedAfterResolved() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = rejectAfter(exception, 100);
        Promise<String> promise2 = Promises.resolved(SUCCESS2);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS2, promise);
    }

    @Test
    public void whenAnyCompleteNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<String> promise = PromiseManager.whenAnyCompleted(list);

        assertFalse(promise.isDone());
    }

    @Test
    public void whenAnyCompleteNullArray() throws Throwable {
        Promise<String>[] array = null;
        Promise<String> promise = PromiseManager.whenAnyCompleted(array);

        assertFalse(promise.isDone());
    }

    @Test
    public void whenAnyCompleteListContainsNulls() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = null;

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2, promise3);

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolved2() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolved3() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2, promise3);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS3, promise3);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolved4() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 100);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2, promise3, promise4);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS3, promise3);
        assertResolves(SUCCESS4, promise4);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolved5() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 100);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 100);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2, promise3, promise4, promise5);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS3, promise3);
        assertResolves(SUCCESS4, promise4);
        assertResolves(SUCCESS5, promise5);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenAnyResolvedArray() throws Throwable {
        Promise<String> promise1 = Promises.resolved(SUCCESS1);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = resolveAfter(SUCCESS3, 100);
        Promise<String> promise4 = resolveAfter(SUCCESS4, 100);
        Promise<String> promise5 = resolveAfter(SUCCESS5, 100);

        Promise<String>[] array = (Promise<String>[])new Promise [] {
                promise1, promise2, promise3, promise4, promise5
        };

        Promise<String> promise = PromiseManager.whenAnyResolved(array);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS3, promise3);
        assertResolves(SUCCESS4, promise4);
        assertResolves(SUCCESS5, promise5);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolvedEventually() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2);

        assertResolves(SUCCESS1, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void whenAnyResolvedRejects() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promises.rejected(exception);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS2, promise);
    }

    @Test
    public void whenAnyResolvedRejectedAfterResolved() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = rejectAfter(exception, 100);
        Promise<String> promise2 = Promises.resolved(SUCCESS2);

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2);

        assertRejects(exception, promise1);
        assertResolves(SUCCESS2, promise2);
        assertResolves(SUCCESS2, promise);
    }

    @Test
    public void whenAnyResolvedNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<String> promise = PromiseManager.whenAnyResolved(list);

        assertFalse(promise.isDone());
    }

    @Test
    public void whenAnyResolvedNullArray() throws Throwable {
        Promise<String>[] array = null;
        Promise<String> promise = PromiseManager.whenAnyResolved(array);

        assertFalse(promise.isDone());
    }

    @Test
    public void whenAnyResolvedListContainsNulls() throws Throwable {
        Promise<String> promise1 = resolveAfter(SUCCESS1, 10);
        Promise<String> promise2 = resolveAfter(SUCCESS2, 100);
        Promise<String> promise3 = null;

        Promise<String> promise = PromiseManager.whenAnyResolved(promise1, promise2, promise3);

        assertResolves(SUCCESS1, promise);
    }
}
