package org.jpromise;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    public final static String DEFAULT_CONTINUATION_EXECUTOR_KEY = "org.jpromise.continuation_executor";
    public final static String DEFAULT_CREATION_EXECUTOR_KEY = "org.jpromise.creation_executor";
    public final static String DEFAULT_FUTURE_EXECUTOR_KEY = "org.jpromise.future_executor";
    public final static String COMMON_POOL_KEY = "common_pool";
    public final static String CURRENT_THREAD_KEY = "current_thread";
    public final static String NEW_THREAD_KEY = "new_thread";


    public final static Executor DEFAULT_CONTINUATION_EXECUTOR;
    public final static Executor DEFAULT_CREATION_EXECUTOR;
    public final static Executor DEFAULT_FUTURE_EXECUTOR;

    static {
        DEFAULT_CONTINUATION_EXECUTOR = ExecutorResolver.resolveBySetting(DEFAULT_CONTINUATION_EXECUTOR_KEY, COMMON_POOL);
        DEFAULT_CREATION_EXECUTOR = ExecutorResolver.resolveBySetting(DEFAULT_CREATION_EXECUTOR_KEY, COMMON_POOL);
        DEFAULT_FUTURE_EXECUTOR = ExecutorResolver.resolveBySetting(DEFAULT_FUTURE_EXECUTOR_KEY, NEW_THREAD);
    }

    private final static Executor pool = Executors.newCachedThreadPool();
}

