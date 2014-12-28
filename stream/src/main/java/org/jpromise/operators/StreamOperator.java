package org.jpromise.operators;

import org.jpromise.PromiseSubscriber;

public abstract class StreamOperator<V_IN, V_OUT> {
    public PromiseSubscriber<V_IN> subscribe(final PromiseSubscriber<? super V_OUT> subscriber) {
        return new PromiseSubscriber<V_IN>() {
            private final OutstandingOperationTracker tracker = new OutstandingOperationTracker();

            @Override
            public void fulfilled(V_IN result) {
                OutstandingOperation operation = tracker.start();
                try {
                    StreamOperator.this.fulfilled(subscriber, result);
                }
                catch (Throwable exception) {
                    subscriber.rejected(exception);
                }
                finally {
                    operation.complete();
                }
            }

            @Override
            public void rejected(Throwable exception) {
                OutstandingOperation operation = tracker.start();
                try {
                    StreamOperator.this.rejected(subscriber, exception);
                }
                catch (Throwable error) {
                    subscriber.rejected(error);
                }
                finally {
                    operation.complete();
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
        };
    }

    protected abstract void fulfilled(PromiseSubscriber<? super V_OUT> subscriber, V_IN result) throws Throwable;

    protected void rejected(PromiseSubscriber<? super V_OUT> subscriber, Throwable exception) throws Throwable {
        subscriber.rejected(exception);
    }

    protected void complete(PromiseSubscriber<? super V_OUT> subscriber) throws Throwable {
        subscriber.complete();
    }
}
