package org.jpromise;

import java.util.concurrent.Callable;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public abstract class AbstractPromiseService implements PromiseService {
    /**
     * {@inheritDoc}
     * @param task {@inheritDoc}
     * @param <V> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public <V> Promise<V> submit(final Callable<V> task) {
        if (task == null) throw new NullPointerException(mustNotBeNull("task"));
        final Deferred<V> deferred = Promise.defer();
        this.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deferred.resolve(task.call());
                }
                catch (Throwable exception) {
                    deferred.reject(exception);
                }
            }
        });
        return deferred.promise();
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
        final Deferred<V> deferred = Promise.defer();
        this.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run();
                    deferred.resolve(result);
                }
                catch (Throwable exception) {
                    deferred.reject(exception);
                }
            }
        });
        return deferred.promise();
    }

    /**
     * {@inheritDoc}
     * @param task {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Promise<Void> submit(final Runnable task) {
        return this.submit(task, null);
    }

    protected abstract void execute(Runnable task);
}
