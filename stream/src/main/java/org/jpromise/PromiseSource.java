package org.jpromise;

import java.util.Arrays;

class PromiseSource<V> extends AbstractPromiseStream<V> {
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
            return Promises.fulfilled();
        }
        return new PromiseSubscription<V>(promises, subscriber);
    }
}
