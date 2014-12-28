package org.jpromise;

/**
 * Indicates the current state of the {@link org.jpromise.Promise}.
 */
public enum PromiseState {
    /**
     * Specifies that the {@link org.jpromise.Promise} is not completed.
     */
    PENDING,
    /**
     * Specifies that the {@link org.jpromise.Promise} has fulfilled successfully.
     */
    FULFILLED,
    /**
     * Specifies that the {@link org.jpromise.Promise} has been rejected.
     */
    REJECTED
}
