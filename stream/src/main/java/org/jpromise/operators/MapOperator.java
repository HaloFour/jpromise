package org.jpromise.operators;

import org.jpromise.OnSubscribe;
import org.jpromise.functions.OnResolvedFunction;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class MapOperator<V_IN, V_APPLIED> extends BoundedStreamOperator<V_IN, V_APPLIED> {
    private final OnResolvedFunction<? super V_IN, ? extends V_APPLIED> function;

    public MapOperator(OnSubscribe<V_IN> subscriber, OnResolvedFunction<? super V_IN, ? extends V_APPLIED> function) {
        super(subscriber);
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        this.function = function;
    }

    @Override
    protected void resolved(BoundedPromiseSubscriber<V_APPLIED> subscriber, V_IN result) throws Throwable {
        subscriber.resolved(function.resolved(result));
    }
}
