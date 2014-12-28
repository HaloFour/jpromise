package org.jpromise.functions;

import org.jpromise.Promise;

/**
 * Represents an operation that will occur on the completion of a promise.
 * @see org.jpromise.Promise#whenCompleted(OnCompleted)
 * @param <V> The result type of the promise.
 */
public interface OnCompleted<V> {
    /**
     * Performs the operation on the completion of the promise.
     * @param promise The promise that has completed.
     * @param result The result of the promise if the promise has fulfilled successfully, otherwise {@code null}.
     * @param exception The exception that caused the promise to be rejected, otherwise {@code null}.
     * @throws Throwable Any exception that might occur during the operation.
     */
    void completed(Promise<V> promise, V result, Throwable exception) throws Throwable;
}
