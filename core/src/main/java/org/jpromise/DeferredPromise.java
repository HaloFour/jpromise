package org.jpromise;

public class DeferredPromise<V> extends AbstractPromise<V> implements Deferred<V> {
    @Override
    public boolean resolve(V result) {
        return set(result);
    }

    @Override
    public boolean reject(Throwable exception) {
        return setException(exception);
    }

    @Override
    public Promise<V> promise() {
        return this;
    }
}