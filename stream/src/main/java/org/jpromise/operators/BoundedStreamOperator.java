package org.jpromise.operators;

import org.jpromise.OnSubscribe;
import org.jpromise.PromiseSubscriber;

import java.util.concurrent.atomic.AtomicLong;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public abstract class BoundedStreamOperator<V_IN, V_OUT> extends StreamOperator<V_IN, V_OUT> {
    public BoundedStreamOperator(OnSubscribe<V_IN> parent) {
        super(parent);
    }

    @Override
    public final void subscribed(PromiseSubscriber<V_OUT> subscriber) {
        if (subscriber == null) throw new IllegalArgumentException(mustNotBeNull("subscriber"));
        BoundedPromiseSubscriberImpl bounded = new BoundedPromiseSubscriberImpl(subscriber);
        super.subscribed(bounded);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void resolved(PromiseSubscriber<V_OUT> subscriber, V_IN result) throws Throwable {
        BoundedPromiseSubscriberImpl bounded = (BoundedPromiseSubscriberImpl) subscriber;
        bounded.increment();
        this.resolved(bounded, result);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void rejected(PromiseSubscriber<V_OUT> subscriber, Throwable exception) throws Throwable {
        BoundedPromiseSubscriberImpl bounded = (BoundedPromiseSubscriberImpl) subscriber;
        bounded.increment();
        this.rejected(bounded, exception);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void complete(PromiseSubscriber<V_OUT> subscriber) throws Throwable {
        BoundedPromiseSubscriberImpl bounded = (BoundedPromiseSubscriberImpl) subscriber;
        bounded.complete();
    }

    protected abstract void resolved(BoundedPromiseSubscriber<V_OUT> subscriber, V_IN result) throws Throwable;

    protected void rejected(BoundedPromiseSubscriber<V_OUT> subscriber, Throwable exception) throws Throwable {
        subscriber.rejected(exception);
    }

    private class BoundedPromiseSubscriberImpl implements BoundedPromiseSubscriber<V_OUT> {
        private final PromiseSubscriber<V_OUT> parent;
        private final AtomicLong counter = new AtomicLong(1);

        public BoundedPromiseSubscriberImpl(PromiseSubscriber<V_OUT> parent) {
            this.parent = parent;
        }

        @Override
        public void omit() {
            decrement();
        }

        @Override
        public void resolved(V_OUT result) {
            parent.resolved(result);
            decrement();
        }

        @Override
        public void rejected(Throwable exception) {
            parent.rejected(exception);
            decrement();
        }

        @Override
        public void complete() {
            decrement();
        }

        public void increment() {
            counter.incrementAndGet();
        }

        private void decrement() {
            if (counter.decrementAndGet() <= 0) {
                parent.complete();
            }
        }
    }
}