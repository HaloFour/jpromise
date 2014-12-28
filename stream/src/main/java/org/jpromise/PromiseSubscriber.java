package org.jpromise;

public interface PromiseSubscriber<V> {
    void fulfilled(V result);
    void rejected(Throwable exception);
    void complete();
}
