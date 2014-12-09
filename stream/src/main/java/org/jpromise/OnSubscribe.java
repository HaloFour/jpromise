package org.jpromise;

interface OnSubscribe<V> {
    void subscribed(PromiseSubscriber<V> subscriber);
}
