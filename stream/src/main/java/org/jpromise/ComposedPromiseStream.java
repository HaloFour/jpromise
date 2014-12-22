package org.jpromise;

import org.jpromise.operators.StreamOperator;

class ComposedPromiseStream<V_IN, V_OUT> extends PromiseStream<V_OUT> {
    private final PromiseStream<V_IN> parent;
    private final StreamOperator<V_IN, V_OUT> operator;

    ComposedPromiseStream(PromiseStream<V_IN> parent, StreamOperator<V_IN, V_OUT> operator) {
        this.parent = parent;
        this.operator = operator;
    }

    @Override
    public Promise<Void> subscribe(PromiseSubscriber<? super V_OUT> subscriber) {
        PromiseSubscriber<V_IN> parentSubscriber = operator.subscribe(subscriber);
        return parent.subscribe(parentSubscriber);
    }
}
