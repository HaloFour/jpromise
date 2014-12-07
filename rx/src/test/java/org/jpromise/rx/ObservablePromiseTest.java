package org.jpromise.rx;

import junit.framework.AssertionFailedError;
import org.jpromise.Promise;
import org.junit.Test;
import rx.Observable;
import rx.observables.BlockingObservable;

import java.util.ArrayList;
import java.util.List;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObservablePromiseTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final String SUCCESS2 = "SUCCESS2";
    private static final String SUCCESS3 = "SUCCESS3";
    private static final String SUCCESS4 = "SUCCESS4";

    @Test
    public void fromObservable() throws Throwable {
        Observable<String> observable = Observable.from(new String[] { SUCCESS1 });

        Promise<String> promise = new ObservablePromise<>(observable);

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void fromObservableError() throws Throwable {
        Throwable exception = new Throwable();
        Observable<String> observable = Observable.error(exception);

        Promise<String> promise = new ObservablePromise<>(observable);

        assertRejects(exception, promise);
    }

    @Test
    public void fromEmptyObservableRejects() throws Throwable {
        Observable<String> observable = Observable.empty();

        Promise<String> promise = new ObservablePromise<>(observable);

        assertRejects(promise);
    }

    @Test
    public void toObservableSingle() throws Throwable {
        Promise<String> promise = Promise.resolved(SUCCESS1);

        BlockingObservable<String> observable = new PromiseObservable<>(promise).toBlocking();

        assertEquals(SUCCESS1, observable.single());
    }

    @Test
    public void toObservableEmpty() throws Throwable {
        List<Promise<String>> promises = new ArrayList<Promise<String>>(0);

        BlockingObservable<List<String>> observable = new PromiseObservable<>(promises).toList().toBlocking();

        List<String> list = observable.single();

        assertEquals(0, list.size());
    }

    @Test
    public void toObservableMany() throws Throwable {
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);
        Promise<String> promise3 = Promise.resolved(SUCCESS3);
        Promise<String> promise4 = Promise.resolved(SUCCESS4);

        BlockingObservable<List<String>> observable = new PromiseObservable<>(promise1, promise2, promise3, promise4).toList().toBlocking();

        List<String> list = observable.single();

        assertEquals(4, list.size());
        assertTrue(list.contains(SUCCESS1));
        assertTrue(list.contains(SUCCESS2));
        assertTrue(list.contains(SUCCESS3));
        assertTrue(list.contains(SUCCESS4));
    }

    @Test
    public void toObservableFails() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);
        Promise<String> promise3 = Promise.resolved(SUCCESS3);
        Promise<String> promise4 = Promise.rejected(exception);

        BlockingObservable<List<String>> observable = new PromiseObservable<>(promise1, promise2, promise3, promise4).toList().toBlocking();

        try {
            List<String> ignored = observable.single();
            throw new AssertionFailedError("observable.single should have thrown an exception.");
        }
        catch (RuntimeException runtimeException) {
            assertEquals(exception, runtimeException.getCause());
        }
    }

    @Test
    public void toObservableIgnoresRejectionsFails() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise1 = Promise.resolved(SUCCESS1);
        Promise<String> promise2 = Promise.resolved(SUCCESS2);
        Promise<String> promise3 = Promise.resolved(SUCCESS3);
        Promise<String> promise4 = Promise.rejected(exception);

        BlockingObservable<List<String>> observable = new PromiseObservable<>(promise1, promise2, promise3, promise4).ignoringRejected().toList().toBlocking();

        List<String> list = observable.single();

        assertEquals(3, list.size());
        assertTrue(list.contains(SUCCESS1));
        assertTrue(list.contains(SUCCESS2));
        assertTrue(list.contains(SUCCESS3));
    }
}
