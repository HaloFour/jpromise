package org.jpromise.functions;

/**
 * Represents an operation that will occur when a promise is resolved successfully.
 * @param <V> The type of the successful result of the promise.
 */
public interface OnResolved<V> {
    /**
     * Performs the operation on the successful resolution of the promise.
     * @param result The result of the promise.
     * @throws Throwable Any exception that might occur during the operation.
     */
    void resolved(V result) throws Throwable;
}
