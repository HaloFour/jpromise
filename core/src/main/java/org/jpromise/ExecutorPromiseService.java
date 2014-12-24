package org.jpromise;

import java.util.concurrent.Executor;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

/**
 * A {@link org.jpromise.PromiseService} that uses the supplied {@link java.util.concurrent.Executor} to execute
 * the supplied tasks.
 */
public class ExecutorPromiseService extends AbstractPromiseService {
    private final Executor executor;

    /**
     * Creates a new {@link org.jpromise.ExecutorPromiseService} using the supplied {@link java.util.concurrent.Executor}
     * to execute the tasks.
     * @param executor The {@link java.util.concurrent.Executor} to use when executing the tasks.
     */
    public ExecutorPromiseService(Executor executor) {
        if (executor == null) throw new NullPointerException(mustNotBeNull("executor"));
        this.executor = executor;
    }

    @Override
    protected void execute(Runnable task) {
        executor.execute(task);
    }
}
