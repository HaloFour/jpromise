package org.jpromise.operators;

public class FilterNullOperator<V> extends BoundedStreamOperator<V, V> {
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
