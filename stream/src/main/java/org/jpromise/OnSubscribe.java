package org.jpromise;

public interface OnSubscribe<V> {
    void subscribed(PromiseSubscriber<V> subscriber);
}
