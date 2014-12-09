package org.jpromise;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public enum PromiseExecutors implements Executor {
    CURRENT_THREAD {
        @Override
        public void execute(Runnable command) {
            if (command != null) {
                command.run();
            }
        }
    },
    NEW_THREAD {
        @Override
        public void execute(Runnable command) {
            if (command != null) {
                Thread thread = new Thread(command);
                thread.setDaemon(true);
                thread.start();
            }
        }
    },
    COMMON_POOL {
        @Override
        public void execute(Runnable command) {
            if (command != null) {
                pool.execute(command);
            }
        }
    };

    private final static ForkJoinPool pool = new ForkJoinPool();
}

