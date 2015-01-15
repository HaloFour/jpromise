package org.jpromise;

import org.jpromise.functions.OnCompleted;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

class PromiseSubscription<V> extends AbstractPromise<Void> {
    private final PromiseSubscriber<? super V> subscriber;
    private final AtomicBoolean done = new AtomicBoolean(false);

    public PromiseSubscription(Iterable<Promise<V>> promises, final PromiseSubscriber<? super V> subscriber) {
        this.subscriber = subscriber;
        PromiseManager
                .whenAllCompleted(promises, new OnCompleted<V>() {
                    @Override
                    public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                        if (done.get()) {
                            return;
                        }
                        switch (promise.state()) {
                            case FULFILLED:
                                subscriber.fulfilled(result);
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
                        if (done.compareAndSet(false, true)) {
                            subscriber.complete();
                            complete(null);
                        }
                    }
                });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (done.compareAndSet(false, true)) {
            subscriber.rejected(new CancellationException());
            subscriber.complete();
            return super.cancel(true);
        }
        return false;
    }
}