package org.jpromise.rx;

import org.jpromise.AbstractPromise;
import org.jpromise.Arg;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

public class ObservablePromise<V> extends AbstractPromise<V> {
    private final Subscription subscription;

    public ObservablePromise(Observable<V> observable) {
        Arg.ensureNotNull(observable, "observable");
        this.subscription = observable.single().subscribe(new Subscriber<V>() {
            private V result;

            @Override
            public synchronized void onCompleted() {
                set(this.result);
            }

            @Override
            public synchronized void onError(Throwable exception) {
                setException(exception);
            }

            @Override
            public synchronized void onNext(V result) {
                this.result = result;
            }
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        subscription.unsubscribe();
        return super.cancel(mayInterruptIfRunning);
    }
}
