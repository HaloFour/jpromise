package org.jpromise.functions;

/**
 * Represents an operation that will occur when a promise is fulfilled successfully that can transform the
 * result into a different value.
 * @param <V1> The type of the successful result of the promise.
 * @param <V2> The type of the transformed result of the promise.
 */
public interface OnFulfilledFunction<V1, V2> {
    /**
     * Performs the operation on the successful fulfillment of the promise and transforms the result
     * into a different value.
     * @param result The result of the promise.
     * @return The transformed value.
     * @throws Throwable Any exception that might occur during the operation.
     */
    V2 fulfilled(V1 result) throws Throwable;
}
