package org.jpromise;

import java.util.concurrent.Executor;

public enum CurrentThreadExecutor implements Executor {
    INSTANCE {
        @Override
        public void execute(Runnable runnable) {
            if (runnable != null) {
                runnable.run();
            }
        }
    }
}

