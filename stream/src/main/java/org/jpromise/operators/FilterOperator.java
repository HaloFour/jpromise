package org.jpromise.operators;

import org.jpromise.OnSubscribe;
import org.jpromise.functions.OnResolvedFunction;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class FilterOperator<V> extends BoundedStreamOperator<V, V> {
    private final OnResolvedFunction<V, Boolean> predicate;

    public FilterOperator(OnSubscribe<V> parent, OnResolvedFunction<V, Boolean> predicate) {
        super(parent);
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        this.predicate = predicate;
    }

    @Override
    protected void resolved(BoundedPromiseSubscriber<V> subscriber, V result) throws Throwable {
        Boolean filter = predicate.resolved(result);
        if (filter != null && filter) {
            subscriber.resolved(result);
        }
        else {
            subscriber.omit();
        }
    }
}
