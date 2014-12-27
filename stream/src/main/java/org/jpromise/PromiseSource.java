package org.jpromise;

import org.jpromise.functions.OnCompleted;

import java.util.Arrays;

class PromiseSource<V> extends PromiseStream<V> {
    private final Iterable<Promise<V>> promises;

    public PromiseSource(Promise<V>[] promises) {
        this(promises != null ? Arrays.asList(promises) : null);
    }

    public PromiseSource(Iterable<Promise<V>> promises) {
        this.promises = promises;
    }

    @Override
    public Promise<Void> subscribe(final PromiseSubscriber<? super V> subscriber) {
        if (promises == null) {
            subscriber.complete();
            return Promises.resolved();
        }
        return PromiseManager
                .whenAllCompleted(promises, new OnCompleted<V>() {
                    @Override
                    public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                        switch (promise.state()) {
                            case RESOLVED:
                                subscriber.resolved(result);
                                break;
                            case REJECTED:
                                subscriber.rejected(exception);
                                break;
                        }
                    }
                })
                .whenCompleted(PromiseExecutors.CURRENT_THREAD, new OnCompleted<Void>() {
                    @Override
                    public void completed(Promise<Void> promise, Void result, Throwable exception) throws Throwable {
                        subscriber.complete();
                    }
                });
    }
}
