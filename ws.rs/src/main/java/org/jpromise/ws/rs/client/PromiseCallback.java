package org.jpromise.ws.rs.client;

import org.jpromise.Deferred;
import org.jpromise.Promise;

import javax.ws.rs.client.InvocationCallback;

/**
 * Callback that can be implemented to receive the asynchronous processing events from the invocation processing.
 * @param <RESPONSE> The type of the response.  It can be either a general-purpose {@link javax.ws.rs.core.Response}
 *                   or the anticipated response entity type.
 */
public abstract class PromiseCallback<RESPONSE> implements InvocationCallback<RESPONSE> {
    private final Deferred<RESPONSE> deferred = Promise.defer();
    /**
     * The {@link org.jpromise.Promise} representing the completion of the asynchronous process.
     */
    public final Promise<RESPONSE> promise = deferred.promise();

    /**
     * {@inheritDoc}
     * @param response {@inheritDoc}
     */
    @Override
    public void completed(RESPONSE response) {
        deferred.resolve(response);
    }

    /**
     * {@inheritDoc}
     * @param throwable {@inheritDoc}
     */
    @Override
    public void failed(Throwable throwable) {
        deferred.reject(throwable);
    }
}
