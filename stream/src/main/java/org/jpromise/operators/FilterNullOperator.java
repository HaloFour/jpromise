package org.jpromise.operators;

import org.jpromise.OnSubscribe;

public class FilterNullOperator<V> extends BoundedStreamOperator<V, V> {
    public FilterNullOperator(OnSubscribe<V> parent) {
        super(parent);
    }

    @Override
    protected void resolved(BoundedPromiseSubscriber<V> subscriber, V result) throws Throwable {
        if (result != null) {
            subscriber.resolved(result);
        }
        else {
            subscriber.omit();
        }
    }
}
