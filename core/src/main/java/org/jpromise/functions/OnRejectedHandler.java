package org.jpromise.functions;

/**
 * Represents an operation that will occur when the promise is rejected in order to handle the rejection
 * with a successful result.
 * @param <E> The type of the exception that caused the promise to reject.
 * @param <V> The type of the expected result of the promise.
 */
public interface OnRejectedHandler<E extends Throwable, V> {
    /**
     * Performs the operation to handle the rejection of the promise and produce a successful result.
     * @param exception The exception that caused the promise to be rejected.
     * @return A successful result for the promise.
     * @throws Throwable Any exception that might occur during the operation.
     */
    V handle(E exception) throws Throwable;
}
