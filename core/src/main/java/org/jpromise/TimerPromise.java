package org.jpromise;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class TimerPromise extends AbstractPromise<Void> {
    private final Timer timer = new Timer();

    public TimerPromise(long timeout, TimeUnit timeUnit) {
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
