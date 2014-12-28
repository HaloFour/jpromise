package org.jpromise.operators;

import org.jpromise.PromiseState;
import org.jpromise.PromiseSubscriber;

public class TakeOperator<V> extends StreamOperator<V, V> {
    private final int count;

    public TakeOperator(int count) {
        this.count = count;
    }

    @Override
    protected void fulfilled(PromiseSubscriber<? super V> subscriber, V result) throws Throwable {
        subscriber.fulfilled(result);
    }

    @Override
    public PromiseSubscriber<V> subscribe(PromiseSubscriber<? super V> subscriber) {
        return super.subscribe(new TakeSubscriber<V>(subscriber, count));
    }

    private static class TakeSubscriber<V> implements PromiseSubscriber<V> {
        private final Object lock = new Object();
        private final OutstandingOperationTracker tracker = new OutstandingOperationTracker();
        private final PromiseSubscriber<? super V> parent;
        private int remaining;

        public TakeSubscriber(PromiseSubscriber<? super V> parent, int remaining) {
            this.parent = parent;
            this.remaining = remaining;
        }

        @Override
        public void fulfilled(V result) {
            completed(PromiseState.FULFILLED, result, null);
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
                if (remaining == 0) return;
                remaining -= 1;
                complete = (remaining == 0);
            }
            OutstandingOperation operation = tracker.start();
            try {
                switch (state) {
                    case FULFILLED:
                        parent.fulfilled(result);
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
                if (remaining == 0) return;
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
