package org.jpromise.operators;

import org.jpromise.OnSubscribe;
import org.jpromise.PromiseSubscriber;
import org.jpromise.functions.OnResolvedFunction;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class FlatMapOperator<V_IN, V_APPLIED> extends StreamOperator<V_IN, V_APPLIED> {
    private final OnResolvedFunction<? super V_IN, ? extends Iterable<? extends V_APPLIED>> function;

    public FlatMapOperator(OnSubscribe<V_IN> parent, OnResolvedFunction<? super V_IN, ? extends Iterable<? extends V_APPLIED>> function) {
        super(parent);
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        this.function = function;
    }

    @Override
    protected void resolved(PromiseSubscriber<V_APPLIED> subscriber, V_IN result) throws Throwable {
        Iterable<? extends V_APPLIED> iterable = function.resolved(result);
        if (iterable != null) {
            for (V_APPLIED value : iterable) {
                subscriber.resolved(value);
            }
        }
    }
}
