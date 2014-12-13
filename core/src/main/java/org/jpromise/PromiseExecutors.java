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
    public final static Executor DEFAULT_CREATION_EXECUTOR;
    public final static Executor DEFAULT_CONTINUATION_EXECUTOR;
    public final static Executor DEFAULT_FUTURE_EXECUTOR;

    static {
        DEFAULT_CONTINUATION_EXECUTOR = ExecutorResolver.resolveBySetting("org.jpromise.continuation_executor", COMMON_POOL);
        DEFAULT_FUTURE_EXECUTOR = ExecutorResolver.resolveBySetting("org.jpromise.future_executor", NEW_THREAD);
        DEFAULT_CREATION_EXECUTOR = ExecutorResolver.resolveBySetting("org.jpromise.creation_executor", COMMON_POOL);
    }

    private final static ForkJoinPool pool = new ForkJoinPool();
}

