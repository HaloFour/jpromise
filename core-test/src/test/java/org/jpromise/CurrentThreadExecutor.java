package org.jpromise;

import java.util.concurrent.Executor;

public class CurrentThreadExecutor implements Executor {
    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
