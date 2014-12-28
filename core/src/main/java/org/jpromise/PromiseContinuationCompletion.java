package org.jpromise;

/**
 * Represents an operation that is performed when the promise continuation is completed.
 */
public interface PromiseContinuationCompletion {
    /**
     * The operation that is performed when the promise continuation has completed.
     * @param source The completed promise that triggered the continuation.
     * @param target The promise which is the target of the continuation which may or may not have been
     *               completed as a result of the continuation.
     * @param result The result of the {@code source} promise if it had fulfilled.
     * @param exception The exception of the {@code source} promise if it had rejected.
     */
    void completed(Promise<?> source, Promise<?> target, Object result, Throwable exception);
}
