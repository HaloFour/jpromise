package org.jpromise.operators;

import org.jpromise.OnSubscribe;
import org.jpromise.PromiseState;
import org.jpromise.PromiseSubscriber;

public class TakeOperator<V> extends StreamOperator<V, V> {
    private final int count;

    public TakeOperator(OnSubscribe<V> parent, int count) {
        super(parent);
        this.count = count;
    }

    @Override
    protected void resolved(PromiseSubscriber<V> subscriber, V result) throws Throwable {
        subscriber.resolved(result);
    }

    @Override
    public void subscribed(PromiseSubscriber<V> subscriber) {
        super.subscribed(new TakeSubscriber<V>(subscriber, count));
    }

    private static class TakeSubscriber<V> implements PromiseSubscriber<V> {
        private final Object lock = new Object();
        private final OutstandingOperationTracker tracker = new OutstandingOperationTracker();
        private final PromiseSubscriber<V> parent;
        private int remaining;

        public TakeSubscriber(PromiseSubscriber<V> parent, int remaining) {
            this.parent = parent;
            this.remaining = remaining;
        }

        @Override
        public void resolved(V result) {
            completed(PromiseState.RESOLVED, result, null);
        }

        @Override
        public void rejected(Throwable exception) {
            completed(PromiseState.REJECTED, null, exception);
        }

        private void completed(PromiseState state, V result, Throwable exception) {
            if (remaining == 0) {
                return;
            }
            boolean complete = false;
            synchronized (lock) {
                if (remaining == 0) {
                    return;
                }
                remaining -= 1;
                complete = (remaining == 0);
            }
            OutstandingOperation operation = tracker.start();
            try {
                switch (state) {
                    case RESOLVED:
                        parent.resolved(result);
                        break;
                    case REJECTED:
                        parent.rejected(exception);
                        break;
                }
            }
            finally {
                operation.complete();
            }
            if (complete) {
                tracker.complete(new Runnable() {
                    @Override
                    public void run() {
                        parent.complete();
                    }
                });
            }
        }

        @Override
        public void complete() {
            if (remaining == 0) {
                return;
            }
            synchronized (lock) {
                if (remaining == 0) {
                    return;
                }
                remaining = 0;
            }
            tracker.complete(new Runnable() {
                @Override
                public void run() {
                    parent.complete();
                }
            });
        }
    }
}
