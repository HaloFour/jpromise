package org.jpromise;

import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnFulfilled;
import org.jpromise.functions.OnFulfilledFunction;

import java.util.Arrays;
import java.util.concurrent.Future;

class GeneratorSource<V> extends PromiseStream<V> {
    private final OnFulfilledFunction<V, ? extends Future<V>> generator;

    public GeneratorSource(OnFulfilledFunction<V, ? extends Future<V>> generator) {
        this.generator = generator;
    }

    @Override
    public Promise<Void> subscribe(final PromiseSubscriber<? super V> subscriber) {
        Deferred<Void> deferred = Promises.defer();
        next(deferred, subscriber, null);
        return deferred.promise();
    }

    private void next(final Deferred<Void> deferred, final PromiseSubscriber<? super V> subscriber, V previous) {
        boolean completed = false;
        try {
            Future<V> future = generator.fulfilled(previous);
            if (future == null) {
                completed = true;
            }
            else {
                Promise<V> promise = Promises.fromFuture(future);
                promise.whenCompleted(new OnCompleted<V>() {
                    @Override
                    public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                        switch (promise.state()) {
                            case FULFILLED:
                                subscriber.fulfilled(result);
                                next(deferred, subscriber, result);
                                break;
                            case REJECTED:
                                subscriber.rejected(exception);
                                subscriber.complete();
                                deferred.fulfill(null);
                                break;
                        }
                    }
                });
            }
        }
        catch (Throwable exception) {
            subscriber.rejected(exception);
            completed = true;
        }
        finally {
            if (completed) {
                subscriber.complete();
                deferred.fulfill(null);
            }
        }
    }
}