package org.jpromise.rx;

import org.jpromise.AbstractPromise;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import java.util.Objects;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class ObservablePromise<V> extends AbstractPromise<V> {
    private final Subscription subscription;

    public ObservablePromise(Observable<V> observable) {
        if (observable == null) throw new IllegalArgumentException(mustNotBeNull("observable"));
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
