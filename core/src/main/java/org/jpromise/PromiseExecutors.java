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
    public final static Executor DEFAULT_CONTINUATION_EXECUTOR = COMMON_POOL;
    public final static Executor DEFAULT_FUTURE_EXECUTOR = NEW_THREAD;

    private final static ForkJoinPool pool = new ForkJoinPool();
}

