package org.jpromise.operators;

import org.jpromise.Promise;
import org.jpromise.Promises;
import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnFulfilledFunction;

import java.util.concurrent.Future;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class ComposeOperator<V_IN, V_COMPOSED> extends BoundedStreamOperator<V_IN, V_COMPOSED> {
    private final OnFulfilledFunction<? super V_IN, ? extends Future<V_COMPOSED>> function;

    public ComposeOperator(OnFulfilledFunction<? super V_IN, ? extends Future<V_COMPOSED>> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        this.function = function;
    }

    @Override
    protected void fulfilled(final BoundedPromiseSubscriber<V_COMPOSED> subscriber, V_IN result) throws Throwable {
        Future<V_COMPOSED> future = function.fulfilled(result);
        if (future == null) {
            subscriber.fulfilled(null);
            return;
        }
        Promise<V_COMPOSED> promise = Promises.fromFuture(future);
        promise.whenCompleted(new OnCompleted<V_COMPOSED>() {
            @Override
            public void completed(Promise<V_COMPOSED> promise, V_COMPOSED result, Throwable exception) throws Throwable {
                switch (promise.state()) {
                    case FULFILLED:
                        subscriber.fulfilled(result);
                        break;
                    case REJECTED:
                        subscriber.rejected(exception);
                        break;
                }
            }
        });
    }
}
