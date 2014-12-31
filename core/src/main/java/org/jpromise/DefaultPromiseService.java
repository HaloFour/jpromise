package org.jpromise;

/**
 * A {@link org.jpromise.PromiseService} that uses the {@link org.jpromise.PromiseExecutors#DEFAULT_CREATION_EXECUTOR}
 * promise executor to execute tasks.
 */
public final class DefaultPromiseService extends AbstractPromiseService {
    /**
     * The singleton instance of the {@link org.jpromise.DefaultPromiseService}.
     */
    public static final PromiseService INSTANCE = new DefaultPromiseService();

    private DefaultPromiseService() { }

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
