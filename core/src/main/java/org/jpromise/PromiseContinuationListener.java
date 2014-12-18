package org.jpromise;

/**
 * Represents an operation to be performed when a continuation is being invoked due to the completion of the source
 * promise.
 */
public interface PromiseContinuationListener {
    /**
     * Performs the operation that is to be performed when a continuation is being invoked.
     * @param source The completed promise that triggered the continuation.
     * @param target The promise that is the target of the continuation.
     * @param result The result of the {@code source} promise if it had resolved.
     * @param exception The exception of the {@code source} promise if it had rejected.
     * @return A {@link org.jpromise.PromiseContinuationCompletion} instance which is invoked when the continuation
     * has completed.
     */
    PromiseContinuationCompletion invokingContinuation(Promise<?> source, Promise<?> target, Object result, Throwable exception);
}
