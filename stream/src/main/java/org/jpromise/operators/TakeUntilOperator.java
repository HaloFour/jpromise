package org.jpromise.operators;

import org.jpromise.Promise;
import org.jpromise.PromiseSubscriber;
import org.jpromise.functions.OnCompleted;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class TakeUntilOperator<V1, V2> extends StreamOperator<V1, V1> {
    private final Promise<V2> promise;

    public TakeUntilOperator(Promise<V2> promise) {
        if (promise == null) throw new IllegalArgumentException(mustNotBeNull("promise"));
        this.promise = promise;
    }

    @Override
    protected void fulfilled(PromiseSubscriber<? super V1> subscriber, V1 result) throws Throwable {
        subscriber.fulfilled(result);
    }

    @Override
    public PromiseSubscriber<V1> subscribe(PromiseSubscriber<? super V1> subscriber) {
        return super.subscribe(new TakeUntilSubscriber<V1, V2>(subscriber, promise));
    }

    private static class TakeUntilSubscriber<V1, V2> implements PromiseSubscriber<V1>, OnCompleted<V2> {
        private final PromiseSubscriber<? super V1> parent;
        private final AtomicBoolean flag;

        public TakeUntilSubscriber(PromiseSubscriber<? super V1> parent, Promise<V2> promise) {
            this.parent = parent;
            flag = new AtomicBoolean(false);
            promise.whenCompleted(this);
        }

        @Override
        public void fulfilled(V1 result) {
            if (!flag.get()) {
                parent.fulfilled(result);
            }
        }

        @Override
        public void rejected(Throwable exception) {
            if (!flag.get()) {
                parent.rejected(exception);
            }
        }

        @Override
        public void complete() {
            if (flag.compareAndSet(false, true)) {
                parent.complete();
            }
        }

        @Override
        public void completed(Promise<V2> promise, V2 result, Throwable exception) throws Throwable {
            if (flag.compareAndSet(false, true)) {
                parent.complete();
            }
        }
    }
}
