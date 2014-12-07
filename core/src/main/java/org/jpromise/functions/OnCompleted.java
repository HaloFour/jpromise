package org.jpromise.functions;

import org.jpromise.Promise;

public interface OnCompleted<V> {
    void completed(Promise<V> promise, V result, Throwable exception) throws Throwable;
}
