package org.jpromise.operators;

import org.jpromise.functions.OnFulfilledFunction;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class MapOperator<V_IN, V_APPLIED> extends BoundedStreamOperator<V_IN, V_APPLIED> {
    private final OnFulfilledFunction<? super V_IN, ? extends V_APPLIED> function;

    public MapOperator(OnFulfilledFunction<? super V_IN, ? extends V_APPLIED> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        this.function = function;
    }

    @Override
    protected void fulfilled(BoundedPromiseSubscriber<V_APPLIED> subscriber, V_IN result) throws Throwable {
        subscriber.fulfilled(function.fulfilled(result));
    }
}
