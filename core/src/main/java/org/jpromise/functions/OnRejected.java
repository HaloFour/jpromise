package org.jpromise.functions;

/**
 * Represents an operation that will occur when a promise is rejected.
 * @param <E> The type of the exception that caused the promise to reject.
 */
public interface OnRejected<E extends Throwable> {
    /**
     * Performs the operation on the rejection of the promise.
     * @param exception The exception that caused the promise to be rejected.
     * @throws Throwable Any exception that might occur during the operation.
     */
    void rejected(E exception) throws Throwable;
}
