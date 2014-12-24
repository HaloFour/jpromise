package org.jpromise;

/**
 * A {@link org.jpromise.PromiseService} that uses the {@link org.jpromise.PromiseExecutors#DEFAULT_CREATION_EXECUTOR}
 * promise executor to execute tasks.
 */
public class DefaultPromiseService extends ExecutorPromiseService {
    /**
     * Creates a new {@link org.jpromise.DefaultPromiseService}.
     */
    public DefaultPromiseService() {
        super(PromiseExecutors.DEFAULT_CREATION_EXECUTOR);
    }
}
