package org.jpromise;

class DeferredPromise<V> implements Deferred<V> {
    private final AbstractPromise<V> promise = new AbstractPromise<V>() { };

    @Override
    public boolean resolve(V result) {
        return promise.complete(result);
    }

    @Override
    public boolean reject(Throwable exception) {
        return promise.completeWithException(exception);
    }

    @Override
    public Promise<V> promise() {
        return promise;
    }
}