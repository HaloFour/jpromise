package org.jpromise.rx;

import org.jpromise.AbstractPromise;
import org.jpromise.Arg;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

public class ObservablePromise<V> extends AbstractPromise<V> {
    private final Subscription subscription;

    public ObservablePromise(Observable<V> observable) {
        Arg.ensureNotNull(observable, "observable");
        this.subscription = observable.single().subscribe(new Action1<V>() {
            @Override
            public void call(V result) {
                complete(result);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                completeWithException(error);
            }
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        subscription.unsubscribe();
        return super.cancel(mayInterruptIfRunning);
    }
}
