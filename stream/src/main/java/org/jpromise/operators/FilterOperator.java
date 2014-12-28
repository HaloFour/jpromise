package org.jpromise.operators;

import org.jpromise.functions.OnFulfilledFunction;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class FilterOperator<V> extends BoundedStreamOperator<V, V> {
    private final OnFulfilledFunction<V, Boolean> predicate;

    public FilterOperator(OnFulfilledFunction<V, Boolean> predicate) {
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        this.predicate = predicate;
    }

    @Override
    protected void fulfilled(BoundedPromiseSubscriber<V> subscriber, V result) throws Throwable {
        Boolean filter = predicate.fulfilled(result);
        if (filter != null && filter) {
            subscriber.fulfilled(result);
        }
        else {
            subscriber.omit();
        }
    }
}
