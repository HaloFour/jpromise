package org.jpromise.rx;

import org.jpromise.Promise;
import org.jpromise.PromiseManager;
import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnResolved;
import rx.Observable;
import rx.Subscriber;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

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

                final Iterable<Promise<?>> subscribed = subscribeWhileIterating(promises, subscriber);
                Promise<Void> completed;
                if (skipRejections) {
                    completed = PromiseManager.whenAllComplete(subscribed);
                }
                else {
                    completed = PromiseManager.whenAllResolved(subscribed);
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

    private static <V> Iterable<Promise<?>> subscribeWhileIterating(final Iterable<Promise<V>> promises, final Subscriber<? super V> subscriber) {
        return new Iterable<Promise<?>>() {
            @Override
            public Iterator<Promise<?>> iterator() {
                final Iterator<Promise<V>> iterator = promises.iterator();
                return new Iterator<Promise<?>>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Promise<?> next() {
                        Promise<V> promise = iterator.next();
                        if (promise == null) {
                            return null;
                        }
                        return promise.then(new OnResolved<V>() {
                            @Override
                            public void resolved(V result) throws Throwable {
                                if (subscriber.isUnsubscribed()) {
                                    return;
                                }
                                subscriber.onNext(result);
                            }
                        });
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }
}
