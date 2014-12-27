package org.jpromise.operators;

import org.jpromise.Promise;
import org.jpromise.PromiseManager;
import org.jpromise.functions.OnCompleted;
import org.jpromise.functions.OnResolvedFunction;

import java.util.concurrent.Future;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class ComposeOperator<V_IN, V_COMPOSED> extends BoundedStreamOperator<V_IN, V_COMPOSED> {
    private final OnResolvedFunction<? super V_IN, ? extends Future<V_COMPOSED>> function;

    public ComposeOperator(OnResolvedFunction<? super V_IN, ? extends Future<V_COMPOSED>> function) {
        if (function == null) throw new IllegalArgumentException(mustNotBeNull("function"));
        this.function = function;
    }

    @Override
    protected void resolved(final BoundedPromiseSubscriber<V_COMPOSED> subscriber, V_IN result) throws Throwable {
        Future<V_COMPOSED> future = function.resolved(result);
        if (future == null) {
            subscriber.resolved(null);
            return;
        }
        Promise<V_COMPOSED> promise = Promise.fromFuture(future);
        promise.whenCompleted(new OnCompleted<V_COMPOSED>() {
            @Override
            public void completed(Promise<V_COMPOSED> promise, V_COMPOSED result, Throwable exception) throws Throwable {
                switch (promise.state()) {
                    case RESOLVED:
                        subscriber.resolved(result);
                        break;
                    case REJECTED:
                        subscriber.rejected(exception);
                        break;
                }
            }
        });
    }
}
