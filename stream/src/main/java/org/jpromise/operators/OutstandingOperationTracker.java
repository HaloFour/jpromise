package org.jpromise.operators;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public final class OutstandingOperationTracker {
    private final AtomicLong outstanding = new AtomicLong(1);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private Runnable runnable;

    public final OutstandingOperation start() {
        outstanding.incrementAndGet();
        return new OutstandingOperation() {
            private final AtomicBoolean closed = new AtomicBoolean(false);

            @Override
            public void complete() {
                if (closed.compareAndSet(false, true)) {
                    end();
                }
            }
        };
    }

    private void end() {
        if (outstanding.decrementAndGet() == 0) {
            runnable.run();
        }
    }

    public final void complete(Runnable runnable) {
        if (runnable == null) throw new IllegalArgumentException(mustNotBeNull("runnable"));
        if (completed.compareAndSet(false, true)) {
            this.runnable = runnable;
            end();
        }
    }
}
