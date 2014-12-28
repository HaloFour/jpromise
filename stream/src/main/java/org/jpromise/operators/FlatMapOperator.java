package org.jpromise.operators;

import org.jpromise.PromiseSubscriber;
import org.jpromise.functions.OnFulfilledFunction;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class FlatMapOperator<V_IN, V_APPLIED> extends StreamOperator<V_IN, V_APPLIED> {
    private final OnFulfilledFunction<? super V_IN, ? extends Iterable<? extends V_APPLIED>> function;

    public FlatMapOperator(OnFulfilledFunction<? super V_IN, ? extends Iterable<? extends V_APPLIED>> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        this.function = function;
    }

    @Override
    protected void fulfilled(PromiseSubscriber<? super V_APPLIED> subscriber, V_IN result) throws Throwable {
        Iterable<? extends V_APPLIED> iterable = function.fulfilled(result);
        if (iterable != null) {
            for (V_APPLIED value : iterable) {
                subscriber.fulfilled(value);
            }
        }
    }
}
