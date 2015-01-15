package org.jpromise;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class TimerPromise extends AbstractPromise<Void> {
    private final Timer timer;

    public TimerPromise(long timeout, TimeUnit timeUnit) {
        this(new Timer(), timeout, timeUnit);
    }

    public TimerPromise(Timer timer, long timeout, TimeUnit timeUnit) {
        this.timer = timer;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerPromise.this.complete(null);
            }
        }, timeUnit.toMillis(timeout));
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (super.cancel(mayInterruptIfRunning)) {
            timer.cancel();
            return true;
        }
        return false;
    }
}
