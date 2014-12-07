package org.jpromise;

import org.jpromise.Promise;

public class PromiseResult<V> {
    public final Promise<V> promise;
    public V result;
    public Throwable exception;

    public PromiseResult(Promise<V> promise) {
        this.promise = promise;
    }

    public V get() throws Throwable {
        if (exception != null) {
            throw exception;
        }
        return result;
    }
}
