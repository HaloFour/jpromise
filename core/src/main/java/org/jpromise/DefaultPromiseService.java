package org.jpromise;

/**
 * A {@link org.jpromise.PromiseService} that uses the {@link org.jpromise.PromiseExecutors#DEFAULT_CREATION_EXECUTOR}
 * promise executor to execute tasks.
 */
public class DefaultPromiseService extends AbstractPromiseService {
    /**
     * Executes the {@link java.lang.Runnable} task on the {@link org.jpromise.PromiseExecutors#DEFAULT_CREATION_EXECUTOR}
     * executor.
     * @param task The command to be executed.
     */
    @Override
    protected void execute(Runnable task) {
        PromiseExecutors.DEFAULT_CREATION_EXECUTOR.execute(task);
    }
}
