package org.jpromise;

import java.util.concurrent.Callable;

/**
 * Represents a service that produces new {@link org.jpromise.Promise} instances to represent the submitted tasks.
 */
public interface PromiseService {
    /**
     * Submits a value-returning task for execution and returns a Future representing the pending results of the task.
     * @param task The task to submit.
     * @param <V> The type of the return value of the task.
     * @return A {@link org.jpromise.Promise} representing the pending completion of the task.
     */
    <V> Promise<V> submit(Callable<V> task);

    /**
     * Submits a {@link java.lang.Runnable} task for execution and returns a {@link org.jpromise.Promise}
     * representing that task.
     * @param task The task to submit.
     * @param result The result to return when the task is completed.
     * @param <V> The type of the return value of the task.
     * @return A {@link org.jpromise.Promise} representing the pending completion of the task.
     */
    <V> Promise<V> submit(Runnable task, V result);

    /**
     * Submits a {@link java.lang.Runnable} task for execution and returns a {@link org.jpromise.Promise}
     * representing that task.
     * @param task The task to submit.
     * @return A {@link org.jpromise.Promise} representing the pending completion of the task.
     */
    Promise<Void> submit(Runnable task);
}
