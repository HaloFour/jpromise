package org.jpromise;

import org.jpromise.functions.OnCompleted;

interface Continuation<V> extends OnCompleted<V> {
    void completed(Promise<V> promise, V result, Throwable exception);
}
