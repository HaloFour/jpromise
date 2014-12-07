package org.jpromise;

public interface Deferred<V> {
    boolean resolve(V result);
    boolean reject(Throwable exception);
    Promise<V> promise();
}
