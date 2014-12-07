package org.jpromise.rx;

import org.jpromise.Deferred;
import org.jpromise.DeferredPromise;
import org.jpromise.Promise;
import org.jpromise.PromiseManager;
import org.jpromise.functions.OnRejected;
import org.jpromise.functions.OnResolved;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

import java.util.Arrays;
import java.util.Iterator;

public class ObservablePromiseManager {
    private ObservablePromiseManager() { }

    public static <V> Promise<V> fromObservable(Observable<V> observable) {
        final Deferred<V> deferred = new DeferredPromise<V>();

        observable.single().subscribe(new Action1<V>() {
            @Override
            public void call(V result) {
                deferred.resolve(result);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                deferred.reject(throwable);
            }
        });

        return deferred.promise();
    }

    @SafeVarargs
    public static <V> Observable<V> toObservable(Promise<V>... promises) {
        if (promises == null || promises.length == 0) {
            return Observable.empty();
        }
        return toObservable(Arrays.asList(promises));
    }

    public static <V> Observable<V> toObservable(final Iterable<Promise<V>> promises) {
        if (promises == null) {
            return Observable.empty();
        }

        return Observable.create(new Observable.OnSubscribe<V>() {
            @Override
            public void call(final Subscriber<? super V> subscriber) {
                PromiseManager.whenAllResolved(subscribeWhileIterating(promises, subscriber))
                        .then(new OnResolved<Void>() {
                            @Override
                            public void resolved(Void result) throws Throwable {
                                subscriber.onCompleted();
                            }
                        })
                        .rejected(new OnRejected<Throwable>() {
                            @Override
                            public void rejected(Throwable exception) throws Throwable {
                                subscriber.onError(exception);
                            }
                        });
            }
        });
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
