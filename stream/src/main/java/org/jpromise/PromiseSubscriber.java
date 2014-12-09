package org.jpromise;

interface PromiseSubscriber<V> {
    void resolved(V result);
    void rejected(Throwable exception);
    void complete();
}
