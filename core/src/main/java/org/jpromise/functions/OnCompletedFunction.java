package org.jpromise.functions;


import org.jpromise.Promise;

/**
 * Represents an operation that will occur when a promise is completed that can transform the
 * result into a different value.
 * @param <V1> The type of the successful result of the promise.
 * @param <V2> The type of the transformed result of the promise.
 */
public interface OnCompletedFunction<V1, V2> {
    /**
     * Performs the operation on the completion of the promise and transforms the result
     * into a different value.
     * @param promise The promise that has completed.
     * @param result The result of the promise if the promise has fulfilled successfully, otherwise {@code null}.
     * @param exception The exception that caused the promise to be rejected, otherwise {@code null}.
     * @return The transformed value.
     * @throws Throwable Any exception that might occur during the operation.
     */
    V2 completed(Promise<V1> promise, V1 result, Throwable exception) throws Throwable;
}