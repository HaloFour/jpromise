package org.jpromise.functions;

/**
 * Represents an operation that will occur when a promise is fulfilled successfully.
 * @param <V> The type of the successful result of the promise.
 */
public interface OnFulfilled<V> {
    /**
     * Performs the operation on the successful fulfillment of the promise.
     * @param result The result of the promise.
     * @throws Throwable Any exception that might occur during the operation.
     */
    void fulfilled(V result) throws Throwable;
}
