package org.jpromise;

import org.jpromise.functions.OnFulfilled;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

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
        final Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        final Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        final Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        final Promise<String> promise4 = Promises.fulfilled(SUCCESS4);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnFulfilled<Void>() {
                    @Override
                    public void fulfilled(Void result) throws Throwable {
                        assertFulfills(SUCCESS1, promise1);
                        assertFulfills(SUCCESS2, promise2);
                        assertFulfills(SUCCESS3, promise3);
                        assertFulfills(SUCCESS4, promise4);
                    }
                });

        assertFulfills(promise);
    }

    @Test
    public void whenAllCompleteEventually() throws Throwable {
        final Promise<String> promise1 = fulfillAfter(SUCCESS1, 10);
        final Promise<String> promise2 = fulfillAfter(SUCCESS2, 10);
        final Promise<String> promise3 = fulfillAfter(SUCCESS3, 10);
        final Promise<String> promise4 = fulfillAfter(SUCCESS4, 10);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnFulfilled<Void>() {
                    @Override
                    public void fulfilled(Void result) throws Throwable {
                        assertFulfills(SUCCESS1, promise1);
                        assertFulfills(SUCCESS2, promise2);
                        assertFulfills(SUCCESS3, promise3);
                        assertFulfills(SUCCESS4, promise4);
                    }
                });

        assertFulfills(promise);
    }

    @Test
    public void whenAllCompleteRejected() throws Throwable {
        final Throwable exception = new Throwable();
        final Promise<String> promise1 = fulfillAfter(SUCCESS1, 10);
        final Promise<String> promise2 = fulfillAfter(SUCCESS2, 10);
        final Promise<String> promise3 = fulfillAfter(SUCCESS3, 10);
        final Promise<String> promise4 = Promises.rejected(exception);

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnFulfilled<Void>() {
                    @Override
                    public void fulfilled(Void result) throws Throwable {
                        assertFulfills(SUCCESS1, promise1);
                        assertFulfills(SUCCESS2, promise2);
                        assertFulfills(SUCCESS3, promise3);
                        assertRejects(exception, promise4);

                    }
                });

        assertFulfills(promise);
    }


    @Test
    public void whenAllCompleteEmptyList() throws Throwable {
        List<Promise<String>> list = new ArrayList<Promise<String>>(0);
        Promise<Void> promise = PromiseManager.whenAllCompleted(list);

        assertFulfills(promise);
    }

    @Test
    public void whenAllCompleteNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<Void> promise = PromiseManager.whenAllCompleted(list);

        assertFulfills(promise);
    }

    @Test
    public void whenAllCompleteNullArray() throws Throwable {
        Promise<String>[] array = null;
        Promise<Void> promise = PromiseManager.whenAllCompleted(array);

        assertFulfills(promise);
    }

    @Test
    public void whenAllCompleteListContainsNull() throws Throwable {
        final Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        final Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        final Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        final Promise<String> promise4 = null;

        Promise<Void> promise = PromiseManager.whenAllCompleted(promise1, promise2, promise3, promise4)
                .then(new OnFulfilled<Void>() {
                    @Override
                    public void fulfilled(Void result) throws Throwable {
                        assertFulfills(SUCCESS1, promise1);
                        assertFulfills(SUCCESS2, promise2);
                        assertFulfills(SUCCESS3, promise3);
                        assertNull(promise4);
                    }
                });

        assertFulfills(promise);
    }

    @Test
    public void whenAllFulfilled() throws Throwable {
        final Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        final Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        final Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        final Promise<String> promise4 = Promises.fulfilled(SUCCESS4);

        Promise<Void> promise = PromiseManager.whenAllFulfilled(promise1, promise2, promise3, promise4)
                .then(new OnFulfilled<Void>() {
                    @Override
                    public void fulfilled(Void result) throws Throwable {
                        assertFulfills(SUCCESS1, promise1);
                        assertFulfills(SUCCESS2, promise2);
                        assertFulfills(SUCCESS3, promise3);
                        assertFulfills(SUCCESS4, promise4);
                    }
                });

        assertFulfills(promise);
    }

    @Test
    public void whenAllFulfilledWithCallback() throws Throwable {
        @SuppressWarnings("unchecked")
        OnFulfilled<String> callback = (OnFulfilled<String>)mock(OnFulfilled.class);

        final Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        final Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        final Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        final Promise<String> promise4 = Promises.fulfilled(SUCCESS4);

        List<Promise<String>> promises = new ArrayList<Promise<String>>(4);
        promises.add(promise1);
        promises.add(promise2);
        promises.add(promise3);
        promises.add(promise4);

        Promise<Void> promise = PromiseManager.whenAllFulfilled(promises, callback)
                .then(new OnFulfilled<Void>() {
                    @Override
                    public void fulfilled(Void result) throws Throwable {
                        assertFulfills(SUCCESS1, promise1);
                        assertFulfills(SUCCESS2, promise2);
                        assertFulfills(SUCCESS3, promise3);
                        assertFulfills(SUCCESS4, promise4);
                    }
                });

        assertFulfills(promise);
        verify(callback, times(4)).fulfilled(anyString());
        verify(callback, times(1)).fulfilled(SUCCESS1);
        verify(callback, times(1)).fulfilled(SUCCESS2);
        verify(callback, times(1)).fulfilled(SUCCESS3);
        verify(callback, times(1)).fulfilled(SUCCESS4);
    }

    @Test
    public void whenAllFulfilledEventually() throws Throwable {
        final Promise<String> promise1 = fulfillAfter(SUCCESS1, 10);
        final Promise<String> promise2 = fulfillAfter(SUCCESS2, 10);
        final Promise<String> promise3 = fulfillAfter(SUCCESS3, 10);
        final Promise<String> promise4 = fulfillAfter(SUCCESS4, 10);

        Promise<Void> promise = PromiseManager.whenAllFulfilled(promise1, promise2, promise3, promise4)
                .then(new OnFulfilled<Void>() {
                    @Override
                    public void fulfilled(Void result) throws Throwable {
                        assertFulfills(SUCCESS1, promise1);
                        assertFulfills(SUCCESS2, promise2);
                        assertFulfills(SUCCESS3, promise3);
                        assertFulfills(SUCCESS4, promise4);
                    }
                });

        assertFulfills(promise);
    }

    @Test
    public void whenAllFulfilledRejected() throws Throwable {
        final Throwable exception = new Throwable();
        final Promise<String> promise1 = fulfillAfter(SUCCESS1, 10);
        final Promise<String> promise2 = fulfillAfter(SUCCESS2, 10);
        final Promise<String> promise3 = fulfillAfter(SUCCESS3, 10);
        final Promise<String> promise4 = Promises.rejected(exception);

        Promise<Void> promise = PromiseManager.whenAllFulfilled(promise1, promise2, promise3, promise4);

        assertRejects(exception, promise);
    }


    @Test
    public void whenAllFulfilledEmptyList() throws Throwable {
        List<Promise<String>> list = new ArrayList<Promise<String>>(0);
        Promise<Void> promise = PromiseManager.whenAllFulfilled(list);

        assertFulfills(promise);
    }

    @Test
    public void whenAllFulfilledNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<Void> promise = PromiseManager.whenAllFulfilled(list);

        assertFulfills(promise);
    }

    @Test
    public void whenAllFulfilledNullArray() throws Throwable {
        Promise<String>[] array = null;
        Promise<Void> promise = PromiseManager.whenAllFulfilled(array);

        assertFulfills(promise);
    }

    @Test
    public void whenAllFulfilledListContainsNull() throws Throwable {
        final Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        final Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        final Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        final Promise<String> promise4 = null;

        Promise<Void> promise = PromiseManager.whenAllFulfilled(promise1, promise2, promise3, promise4)
                .then(new OnFulfilled<Void>() {
                    @Override
                    public void fulfilled(Void result) throws Throwable {
                        assertFulfills(SUCCESS1, promise1);
                        assertFulfills(SUCCESS2, promise2);
                        assertFulfills(SUCCESS3, promise3);
                        assertNull(promise4);
                    }
                });

        assertFulfills(promise);
    }

    @Test
    public void whenAnyComplete2() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyComplete3() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = fulfillAfter(SUCCESS3, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2, promise3);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS3, promise3);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyComplete4() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = fulfillAfter(SUCCESS3, 100);
        Promise<String> promise4 = fulfillAfter(SUCCESS4, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2, promise3, promise4);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS3, promise3);
        assertFulfills(SUCCESS4, promise4);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyComplete5() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = fulfillAfter(SUCCESS3, 100);
        Promise<String> promise4 = fulfillAfter(SUCCESS4, 100);
        Promise<String> promise5 = fulfillAfter(SUCCESS5, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2, promise3, promise4, promise5);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS3, promise3);
        assertFulfills(SUCCESS4, promise4);
        assertFulfills(SUCCESS5, promise5);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenAnyCompleteArray() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = fulfillAfter(SUCCESS3, 100);
        Promise<String> promise4 = fulfillAfter(SUCCESS4, 100);
        Promise<String> promise5 = fulfillAfter(SUCCESS5, 100);

        Promise<String>[] array = (Promise<String>[])new Promise [] {
                promise1, promise2, promise3, promise4, promise5
        };

        Promise<String> promise = PromiseManager.whenAnyCompleted(array);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS3, promise3);
        assertFulfills(SUCCESS4, promise4);
        assertFulfills(SUCCESS5, promise5);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyCompleteEventually() throws Throwable {
        Promise<String> promise1 = fulfillAfter(SUCCESS1, 10);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyCompleteRejects() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promises.rejected(exception);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2);

        assertRejects(exception, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertRejects(exception, promise);
    }

    @Test
    public void whenAnyCompleteRejectedAfterFulfilled() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = rejectAfter(exception, 100);
        Promise<String> promise2 = Promises.fulfilled(SUCCESS2);

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2);

        assertRejects(exception, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS2, promise);
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
        Promise<String> promise1 = fulfillAfter(SUCCESS1, 10);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = null;

        Promise<String> promise = PromiseManager.whenAnyCompleted(promise1, promise2, promise3);

        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyFulfilled2() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyFulfilled(promise1, promise2);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyFulfilled3() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = fulfillAfter(SUCCESS3, 100);

        Promise<String> promise = PromiseManager.whenAnyFulfilled(promise1, promise2, promise3);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS3, promise3);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyFulfilled4() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = fulfillAfter(SUCCESS3, 100);
        Promise<String> promise4 = fulfillAfter(SUCCESS4, 100);

        Promise<String> promise = PromiseManager.whenAnyFulfilled(promise1, promise2, promise3, promise4);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS3, promise3);
        assertFulfills(SUCCESS4, promise4);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyFulfilled5() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = fulfillAfter(SUCCESS3, 100);
        Promise<String> promise4 = fulfillAfter(SUCCESS4, 100);
        Promise<String> promise5 = fulfillAfter(SUCCESS5, 100);

        Promise<String> promise = PromiseManager.whenAnyFulfilled(promise1, promise2, promise3, promise4, promise5);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS3, promise3);
        assertFulfills(SUCCESS4, promise4);
        assertFulfills(SUCCESS5, promise5);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void whenAnyFulfilledArray() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = fulfillAfter(SUCCESS3, 100);
        Promise<String> promise4 = fulfillAfter(SUCCESS4, 100);
        Promise<String> promise5 = fulfillAfter(SUCCESS5, 100);

        Promise<String>[] array = (Promise<String>[])new Promise [] {
                promise1, promise2, promise3, promise4, promise5
        };

        Promise<String> promise = PromiseManager.whenAnyFulfilled(array);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS3, promise3);
        assertFulfills(SUCCESS4, promise4);
        assertFulfills(SUCCESS5, promise5);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyFulfilledEventually() throws Throwable {
        Promise<String> promise1 = fulfillAfter(SUCCESS1, 10);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyFulfilled(promise1, promise2);

        assertFulfills(SUCCESS1, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void whenAnyFulfilledRejects() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promises.rejected(exception);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);

        Promise<String> promise = PromiseManager.whenAnyFulfilled(promise1, promise2);

        assertRejects(exception, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS2, promise);
    }

    @Test
    public void whenAnyFulfilledRejectedAfterFulfilled() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = rejectAfter(exception, 100);
        Promise<String> promise2 = Promises.fulfilled(SUCCESS2);

        Promise<String> promise = PromiseManager.whenAnyFulfilled(promise1, promise2);

        assertRejects(exception, promise1);
        assertFulfills(SUCCESS2, promise2);
        assertFulfills(SUCCESS2, promise);
    }

    @Test
    public void whenAnyFulfilledNullList() throws Throwable {
        List<Promise<String>> list = null;
        Promise<String> promise = PromiseManager.whenAnyFulfilled(list);

        assertFalse(promise.isDone());
    }

    @Test
    public void whenAnyFulfilledNullArray() throws Throwable {
        Promise<String>[] array = null;
        Promise<String> promise = PromiseManager.whenAnyFulfilled(array);

        assertFalse(promise.isDone());
    }

    @Test
    public void whenAnyFulfilledListContainsNulls() throws Throwable {
        Promise<String> promise1 = fulfillAfter(SUCCESS1, 10);
        Promise<String> promise2 = fulfillAfter(SUCCESS2, 100);
        Promise<String> promise3 = null;

        Promise<String> promise = PromiseManager.whenAnyFulfilled(promise1, promise2, promise3);

        assertFulfills(SUCCESS1, promise);
    }
}
