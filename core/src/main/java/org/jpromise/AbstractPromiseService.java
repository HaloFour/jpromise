package org.jpromise;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

/**
 * Provides default implementation of {@link org.jpromise.PromiseService} execution methods.
 */
public abstract class AbstractPromiseService implements PromiseService {
    /**
     * {@inheritDoc}
     * @param task {@inheritDoc}
     * @param <V> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <V> Promise<V> submit(Callable<V> task) {
        if (task == null) throw new NullPointerException(mustNotBeNull("task"));
        RunnablePromise<V> promise = new RunnablePromise<V>(task);
        this.execute(promise);
        return promise;
    }

    /**
     * {@inheritDoc}
     * @param task {@inheritDoc}
     * @param result {@inheritDoc}
     * @param <V> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <V> Promise<V> submit(final Runnable task, final V result) {
        if (task == null) throw new NullPointerException(mustNotBeNull("task"));
        RunnablePromise<V> promise = new RunnablePromise<V>(task, result);
        this.execute(promise);
        return promise;
    }

    /**
     * {@inheritDoc}
     * @param task {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Promise<Void> submit(Runnable task) {
        return this.submit(task, null);
    }

    /**
     * Must be overridden by implementers to supply the method through which the commands are scheduled
     * and executed.
     * @param task The command to be executed.
     */
    protected abstract void execute(Runnable task);
}
