package org.jpromise.rx;

import junit.framework.AssertionFailedError;
import org.jpromise.Deferred;
import org.jpromise.Promise;
import org.jpromise.Promises;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.BlockingObservable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ObservablePromiseTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String SUCCESS3 = "SUCCESS3";
    private static final String SUCCESS4 = "SUCCESS4";
    private static final String SUCCESS5 = "SUCCESS5";

    @Test
    public void fromObservable() throws Throwable {
        Observable<String> observable = Observable.from(new String[] { SUCCESS1 });

        Promise<String> promise = new ObservablePromise<String>(observable);

        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void fromObservableError() throws Throwable {
        Throwable exception = new Throwable();
        Observable<String> observable = Observable.error(exception);

        Promise<String> promise = new ObservablePromise<String>(observable);

        assertRejects(exception, promise);
    }

    @Test
    public void fromEmptyObservableRejects() throws Throwable {
        Observable<String> observable = Observable.empty();

        Promise<String> promise = new ObservablePromise<String>(observable);

        assertRejects(promise);
    }

    @Test
    public void fromObservableCancel() throws Throwable {
        Observable<String> observable = Observable.timer(100, TimeUnit.MILLISECONDS)
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long aLong) {
                        return SUCCESS1;
                    }
                })
                .take(1);

        Promise<String> promise = new ObservablePromise<String>(observable);
        assertTrue(promise.cancel(true));
        assertRejects(CancellationException.class, promise);
    }

    @Test
    public void toObservableSingle() throws Throwable {
        Promise<String> promise = Promises.fulfilled(SUCCESS1);

        BlockingObservable<String> observable = new PromiseObservable<String>(promise)
                .toBlocking();

        assertEquals(SUCCESS1, observable.single());
    }

    @Test
    public void toObservableEmptyList() throws Throwable {
        List<Promise<String>> promises = new ArrayList<Promise<String>>(0);

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promises).toList().toBlocking();

        List<String> list = observable.single();

        assertEquals(0, list.size());
    }

    @Test
    public void toObservableNullList() throws Throwable {
        List<Promise<String>> promises = null;

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promises).toList().toBlocking();

        List<String> list = observable.single();

        assertEquals(0, list.size());
    }

    @Test
    public void toObservable1() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promise1)
                .toList()
                .toBlocking();

        List<String> list = observable.single();

        assertEquals(1, list.size());
        assertTrue(list.contains(SUCCESS1));
    }

    @Test
    public void toObservable2() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = Promises.fulfilled(SUCCESS2);

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promise1, promise2)
                .toList()
                .toBlocking();

        List<String> list = observable.single();

        assertEquals(2, list.size());
        assertTrue(list.contains(SUCCESS1));
        assertTrue(list.contains(SUCCESS2));
    }

    @Test
    public void toObservable3() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        Promise<String> promise3 = Promises.fulfilled(SUCCESS3);

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promise1, promise2, promise3)
                .toList()
                .toBlocking();

        List<String> list = observable.single();

        assertEquals(3, list.size());
        assertTrue(list.contains(SUCCESS1));
        assertTrue(list.contains(SUCCESS2));
        assertTrue(list.contains(SUCCESS3));
    }

    @Test
    public void toObservable4() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        Promise<String> promise4 = Promises.fulfilled(SUCCESS4);

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promise1, promise2, promise3, promise4)
                .toList()
                .toBlocking();

        List<String> list = observable.single();

        assertEquals(4, list.size());
        assertTrue(list.contains(SUCCESS1));
        assertTrue(list.contains(SUCCESS2));
        assertTrue(list.contains(SUCCESS3));
        assertTrue(list.contains(SUCCESS4));
    }

    @Test
    public void toObservable5() throws Throwable {
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        Promise<String> promise4 = Promises.fulfilled(SUCCESS4);
        Promise<String> promise5 = Promises.fulfilled(SUCCESS5);

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promise1, promise2, promise3, promise4, promise5)
                .toList()
                .toBlocking();

        List<String> list = observable.single();

        assertEquals(5, list.size());
        assertTrue(list.contains(SUCCESS1));
        assertTrue(list.contains(SUCCESS2));
        assertTrue(list.contains(SUCCESS3));
        assertTrue(list.contains(SUCCESS4));
        assertTrue(list.contains(SUCCESS5));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void toObservableFromArray() throws Throwable {
        Promise<String>[] array = (Promise<String>[])new Promise[] {
                Promises.fulfilled(SUCCESS1),
                Promises.fulfilled(SUCCESS2),
                Promises.fulfilled(SUCCESS3),
                Promises.fulfilled(SUCCESS4),
                Promises.fulfilled(SUCCESS5)
        };

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(array)
                .toList()
                .toBlocking();

        List<String> list = observable.single();

        assertEquals(5, list.size());
        assertTrue(list.contains(SUCCESS1));
        assertTrue(list.contains(SUCCESS2));
        assertTrue(list.contains(SUCCESS3));
        assertTrue(list.contains(SUCCESS4));
        assertTrue(list.contains(SUCCESS5));
    }

    @Test
    public void toObservableManyToSingleFails() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        Promise<String> promise4 = Promises.rejected(exception);

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promise1, promise2, promise3, promise4)
                .toList()
                .toBlocking();

        try {
            List<String> ignored = observable.single();
            throw new AssertionFailedError("observable.single should have thrown an exception.");
        }
        catch (RuntimeException runtimeException) {
            assertEquals(exception, runtimeException.getCause());
        }
    }

    @Test
    public void toObservableFilterRejected() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promises.fulfilled(SUCCESS1);
        Promise<String> promise2 = Promises.fulfilled(SUCCESS2);
        Promise<String> promise3 = Promises.fulfilled(SUCCESS3);
        Promise<String> promise4 = Promises.rejected(exception);

        BlockingObservable<List<String>> observable = new PromiseObservable<String>(promise1, promise2, promise3, promise4)
                .filterRejected()
                .toList()
                .toBlocking();

        List<String> list = observable.single();

        assertEquals(3, list.size());
        assertTrue(list.contains(SUCCESS1));
        assertTrue(list.contains(SUCCESS2));
        assertTrue(list.contains(SUCCESS3));
    }

    @Test
    public void toObservableImmediatelyUnsubscribes() throws Throwable {
        Deferred<String> deferred1 = Promises.defer();
        Deferred<String> deferred2 = Promises.defer();
        Deferred<String> deferred3 = Promises.defer();
        Deferred<String> deferred4 = Promises.defer();
        Promise<String> promise1 = deferred1.promise();
        Promise<String> promise2 = deferred2.promise();
        Promise<String> promise3 = deferred3.promise();
        Promise<String> promise4 = deferred4.promise();

        @SuppressWarnings("unchecked")
        Action1<String> action = mock(Action1.class);

        PromiseObservable<String> observable = new PromiseObservable<String>(promise1, promise2, promise3, promise4);
        observable.subscribe(action).unsubscribe();

        deferred1.fulfill(SUCCESS1);
        deferred2.fulfill(SUCCESS2);
        deferred3.fulfill(SUCCESS3);
        deferred4.fulfill(SUCCESS4);

        verify(action, never()).call(anyString());
    }
}
