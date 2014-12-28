package org.jpromise.operators;

import org.jpromise.PromiseSubscriber;

import java.util.concurrent.atomic.AtomicLong;

public abstract class BoundedStreamOperator<V_IN, V_OUT> extends StreamOperator<V_IN, V_OUT> {
    @Override
    public PromiseSubscriber<V_IN> subscribe(final PromiseSubscriber<? super V_OUT> subscriber) {
        BoundedPromiseSubscriberImpl impl = new BoundedPromiseSubscriberImpl(subscriber);
        return super.subscribe(new BoundedPromiseSubscriberImpl(subscriber));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void fulfilled(PromiseSubscriber<? super V_OUT> subscriber, V_IN result) throws Throwable {
        BoundedPromiseSubscriberImpl bounded = (BoundedPromiseSubscriberImpl) subscriber;
        bounded.increment();
        this.fulfilled(bounded, result);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void rejected(PromiseSubscriber<? super V_OUT> subscriber, Throwable exception) throws Throwable {
        BoundedPromiseSubscriberImpl bounded = (BoundedPromiseSubscriberImpl) subscriber;
        bounded.increment();
        this.rejected(bounded, exception);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void complete(PromiseSubscriber<? super V_OUT> subscriber) throws Throwable {
        BoundedPromiseSubscriberImpl bounded = (BoundedPromiseSubscriberImpl) subscriber;
        bounded.complete();
    }

    protected abstract void fulfilled(BoundedPromiseSubscriber<V_OUT> subscriber, V_IN result) throws Throwable;

    protected void rejected(BoundedPromiseSubscriber<V_OUT> subscriber, Throwable exception) throws Throwable {
        subscriber.rejected(exception);
    }

    private class BoundedPromiseSubscriberImpl implements BoundedPromiseSubscriber<V_OUT> {
        private final PromiseSubscriber<? super V_OUT> parent;
        private final AtomicLong counter = new AtomicLong(1);

        public BoundedPromiseSubscriberImpl(PromiseSubscriber<? super V_OUT> parent) {
            this.parent = parent;
        }

        @Override
        public void omit() {
            decrement();
        }

        @Override
        public void fulfilled(V_OUT result) {
            parent.fulfilled(result);
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