package org.jpromise.rx;

import org.jpromise.Promise;
import org.jpromise.PromiseExecutors;
import org.jpromise.PromiseManager;
import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnFulfilled;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PromiseObservable<V> extends Observable<V> {
    private final Iterable<Promise<V>> promises;

    private static <V> Iterable<Promise<V>> toList(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4, Promise<V> promise5) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(5);
        list.add(promise1);
        list.add(promise2);
        list.add(promise3);
        list.add(promise4);
        list.add(promise5);
        return list;
    }

    public PromiseObservable(Promise<V> promise) {
        this(toList(promise, null, null, null, null));
    }

    public PromiseObservable(Promise<V> promise1, Promise<V> promise2) {
        this(toList(promise1, promise2, null, null, null));
    }

    public PromiseObservable(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3) {
        this(toList(promise1, promise2, promise3, null, null));
    }

    public PromiseObservable(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4) {
        this(toList(promise1, promise2, promise3, promise4, null));
    }

    public PromiseObservable(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4, Promise<V> promise5) {
        this(toList(promise1, promise2, promise3, promise4, promise5));
    }

    public PromiseObservable(Promise<V>[] promises) {
        this(promises != null ? Arrays.asList(promises) : null, false);
    }

    public PromiseObservable(final Iterable<Promise<V>> promises) {
        this(promises, false);
    }

    private PromiseObservable(final Iterable<Promise<V>> promises, final boolean skipRejections) {
        super(new OnSubscribe<V>() {
            @Override
            public void call(final Subscriber<? super V> subscriber) {
                if (promises == null) {
                    subscriber.onCompleted();
                    return;
                }

                Promise<Void> completed;
                if (skipRejections) {
                    completed = PromiseManager.whenAllCompleted(promises, PromiseExecutors.CURRENT_THREAD, new OnCompleted<V>() {
                        @Override
                        public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                            if (promise.isFulfilled() && !subscriber.isUnsubscribed()) {
                                subscriber.onNext(result);
                            }
                        }
                    });
                }
                else {
                    completed = PromiseManager.whenAllFulfilled(promises, PromiseExecutors.CURRENT_THREAD, new OnFulfilled<V>() {
                        @Override
                        public void fulfilled(V result) throws Throwable {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(result);
                            }
                        }
                    });
                }

                completed.whenCompleted(PromiseExecutors.CURRENT_THREAD, new OnCompleted<Void>() {
                    @Override
                    public void completed(Promise<Void> promise, Void result, Throwable exception) throws Throwable {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        switch (promise.state()) {
                            case FULFILLED:
                                subscriber.onCompleted();
                                break;
                            case REJECTED:
                                subscriber.onError(exception);
                                break;
                        }
                    }
                });
            }
        });
        this.promises = promises;
    }

    public PromiseObservable<V> filterRejected() {
        return new PromiseObservable<V>(promises, true) { };
    }
}
