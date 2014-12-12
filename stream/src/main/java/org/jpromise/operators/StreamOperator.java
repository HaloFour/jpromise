package org.jpromise.operators;

import org.jpromise.OnSubscribe;
import org.jpromise.PromiseSubscriber;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public abstract class StreamOperator<V_IN, V_OUT> implements OnSubscribe<V_OUT> {
    private final OnSubscribe<V_IN> parent;

    public StreamOperator(OnSubscribe<V_IN> parent) {
        if (parent == null) throw new IllegalArgumentException(mustNotBeNull("parent"));
        this.parent = parent;
    }

    @Override
    public void subscribed(final PromiseSubscriber<V_OUT> subscriber) {
        parent.subscribed(new PromiseSubscriber<V_IN>() {
            private final OutstandingOperationTracker tracker = new OutstandingOperationTracker();

            @Override
            public void resolved(V_IN result) {
                try (OutstandingOperation operation = tracker.start()) {
                    try {
                        StreamOperator.this.resolved(subscriber, result);
                    }
                    catch (Throwable exception) {
                        subscriber.rejected(exception);
                    }
                }
            }

            @Override
            public void rejected(Throwable exception) {
                try (OutstandingOperation operation = tracker.start()) {
                    try {
                        StreamOperator.this.rejected(subscriber, exception);
                    }
                    catch (Throwable error) {
                        subscriber.rejected(error);
                    }
                }
            }

            @Override
            public void complete() {
                tracker.complete(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            StreamOperator.this.complete(subscriber);
                        }
                        catch (Throwable exception) {
                            subscriber.rejected(exception);
                            subscriber.complete();
                        }
                    }
                });
            }
        });
    }

    protected abstract void resolved(PromiseSubscriber<V_OUT> subscriber, V_IN result) throws Throwable;

    protected void rejected(PromiseSubscriber<V_OUT> subscriber, Throwable exception) throws Throwable {
        subscriber.rejected(exception);
    }

    protected void complete(PromiseSubscriber<V_OUT> subscriber) throws Throwable {
        subscriber.complete();
    }
}
