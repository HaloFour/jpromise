package org.jpromise.operators;

public class FilterNullOperator<V> extends BoundedStreamOperator<V, V> {
    @Override
    protected void fulfilled(BoundedPromiseSubscriber<V> subscriber, V result) throws Throwable {
        if (result != null) {
            subscriber.fulfilled(result);
        }
        else {
            subscriber.omit();
        }
    }
}
