package org.jpromise;

/**
 * Represents the deferred result of an operation through which a {@link org.jpromise.Promise} is completed.
 * @param <V> The result type of the deferred operation.
 */
public interface Deferred<V> {
    /**
     * Resolves the deferred operation with the specified result.
     * @param result The result of the deferred operation.
     * @return {@code true} if the deferred operation was resolved; otherwise, {@code false}.
     */
    boolean resolve(V result);

    /**
     * Rejects the deferred operation with the specified exception.
     * @param exception The exception causing the rejection of the deferred operation.
     * @return {@code true} if the deferred operation was rejected; otherwise, {@code false}.
     */
    boolean reject(Throwable exception);

    /**
     * Returns the {@link org.jpromise.Promise} associated with this deferred operation.
     * @return The promise associated with this deferred operation.
     */
    Promise<V> promise();
}
