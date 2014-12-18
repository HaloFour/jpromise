package org.jpromise;

/**
 * Represents an operation that is performed when a continuation is composed.
 */
public interface PromiseCompositionListener {
    /**
     * Perform an operation when a promise continuation is being composed.
     * @param source The promise on which the continuation is bring composed.
     * @param target The promise which is the target of the continuation.
     * @return A {@link org.jpromise.PromiseCompositionListener} instance which is invoked when the continuation
     * is to be invoked.
     */
    PromiseContinuationListener composingContinuation(Promise<?> source, Promise<?> target);
}
