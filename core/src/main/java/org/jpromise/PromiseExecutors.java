package org.jpromise;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The factory of {@link java.util.concurrent.Executor} instances which are used to execute when promises are
 * created or continuations are composed.
 */
public enum PromiseExecutors implements Executor {
    /**
     * An {@link java.util.concurrent.Executor} which executes the operation immediately using the same thread that
     * scheduled the operation.
     */
    CURRENT_THREAD {
        @Override
        public void execute(Runnable command) {
            if (command != null) {
                command.run();
            }
        }
    },
    /**
     * An {@link java.util.concurrent.Executor} which executes the operation immediately using a new daemon thread.
     */
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
    /**
     * An {@link java.util.concurrent.Executor} which schedules the operation to be executed on a common thread pool.
     */
    COMMON_POOL {
        @Override
        public void execute(Runnable command) {
            if (command != null) {
                pool.execute(command);
            }
        }
    };
    /**
     * The property key used to specify the default {@link java.util.concurrent.Executor} for promise continuations.
     */
    public final static String DEFAULT_CONTINUATION_EXECUTOR_KEY = "org.jpromise.continuation_executor";
    /**
     * The property key used to specify the default {@link java.util.concurrent.Executor} for newly created promises.
     */
    public final static String DEFAULT_CREATION_EXECUTOR_KEY = "org.jpromise.creation_executor";
    /**
     * The property key used to specify the default {@link java.util.concurrent.Executor} for blocking on an existing
     * {@link java.util.concurrent.Future} instance to wait for completion.
     */
    public final static String DEFAULT_FUTURE_EXECUTOR_KEY = "org.jpromise.future_executor";
    /**
     * The property key used to specify that the default {@link java.util.concurrent.Executor} is the
     * {@link org.jpromise.PromiseExecutors#COMMON_POOL}.
     */
    public final static String COMMON_POOL_KEY = "common_pool";
    /**
     * The property key used to specify that the default {@link java.util.concurrent.Executor} is the
     * {@link org.jpromise.PromiseExecutors#CURRENT_THREAD}.
     */
    public final static String CURRENT_THREAD_KEY = "current_thread";
    /**
     * The property key used to specify that the default {@link java.util.concurrent.Executor} is the
     * {@link org.jpromise.PromiseExecutors#NEW_THREAD}.
     */
    public final static String NEW_THREAD_KEY = "new_thread";

    /**
     * Returns the default {@link java.util.concurrent.Executor} used when scheduling a composed promise continuation.
     */
    public final static Executor DEFAULT_CONTINUATION_EXECUTOR;
    /**
     * Returns the default {@link java.util.concurrent.Executor} used when creating a new promise using
     * the {@link org.jpromise.DefaultPromiseService} promise service.
     */
    public final static Executor DEFAULT_CREATION_EXECUTOR;
    /**
     * Returns the default {@link java.util.concurrent.Executor} used when creating a promise from an existing
     * {@link java.util.concurrent.Future} instance.
     */
    public final static Executor DEFAULT_FUTURE_EXECUTOR;

    static {
        DEFAULT_CONTINUATION_EXECUTOR = ExecutorResolver.resolveBySetting(DEFAULT_CONTINUATION_EXECUTOR_KEY, COMMON_POOL);
        DEFAULT_CREATION_EXECUTOR = ExecutorResolver.resolveBySetting(DEFAULT_CREATION_EXECUTOR_KEY, COMMON_POOL);
        DEFAULT_FUTURE_EXECUTOR = ExecutorResolver.resolveBySetting(DEFAULT_FUTURE_EXECUTOR_KEY, NEW_THREAD);
    }

    private final static Executor pool = Executors.newCachedThreadPool();
    private final static ThreadLocal<Executor> contextExecutor = new ThreadLocal<Executor>();

    /**
     * Gets the {@link java.util.concurrent.Executor} associated with the current thread.  If no specific instance
     * has been assigned by calling {@link org.jpromise.PromiseExecutors#setContextExecutor(java.util.concurrent.Executor)}
     * then this method will return {@link org.jpromise.PromiseExecutors#DEFAULT_CONTINUATION_EXECUTOR}.
     * @return The {@link java.util.concurrent.Executor} associated with the current thread.
     */
    public static Executor getContextExecutor() {
        Executor executor = contextExecutor.get();
        if (executor == null) {
            executor = DEFAULT_CONTINUATION_EXECUTOR;
        }
        return executor;
    }

    /**
     * Sets the {@link java.util.concurrent.Executor} to be associated with the current thread which will be used by
     * default when composing promise continuations.
     * @param executor The {@link java.util.concurrent.Executor} to be associated with the current thread.
     * @return The previous {@link java.util.concurrent.Executor} that was associated with the current thread.
     */
    public static Executor setContextExecutor(Executor executor) {
        Executor previous = getContextExecutor();
        if (executor == null) {
            contextExecutor.remove();
        }
        else {
            contextExecutor.set(executor);
        }
        return previous;
    }
}

