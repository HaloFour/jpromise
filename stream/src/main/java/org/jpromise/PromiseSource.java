package org.jpromise;

import org.jpromise.functions.OnCompleted;

class PromiseSource<V> implements OnSubscribe<V> {
    private final Iterable<Promise<V>> promises;

    public PromiseSource(Iterable<Promise<V>> promises) {
        this.promises = promises;
    }

    @Override
    public void subscribed(final PromiseSubscriber<V> subscriber) {
        if (promises == null) {
            subscriber.complete();
            return;
        }
        PromiseManager
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
