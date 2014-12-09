package org.jpromise.rx;

import org.jpromise.Promise;
import org.jpromise.PromiseManager;
import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnResolved;
import rx.Observable;
import rx.Subscriber;

import java.util.Arrays;

public class PromiseObservable<V> extends Observable<V> {
    private final Iterable<Promise<V>> promises;

    @SafeVarargs
    public PromiseObservable(Promise<V>... promises) {
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
                    completed = PromiseManager.whenAllCompleted(promises, new OnCompleted<V>() {
                        @Override
                        public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                            if (promise.isResolved() && !subscriber.isUnsubscribed()) {
                                subscriber.onNext(result);
                            }
                        }
                    });
                }
                else {
                    completed = PromiseManager.whenAllResolved(promises, new OnResolved<V>() {
                        @Override
                        public void resolved(V result) throws Throwable {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(result);
                            }
                        }
                    });
                }

                completed.whenCompleted(new OnCompleted<Void>() {
                    @Override
                    public void completed(Promise<Void> promise, Void result, Throwable exception) throws Throwable {
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }
                        switch (promise.state()) {
                            case RESOLVED:
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

    public PromiseObservable<V> ignoringRejected() {
        return new PromiseObservable<V>(promises, true) { };
    }
}
