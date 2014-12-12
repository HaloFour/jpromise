package org.jpromise.operators;

import org.jpromise.PromiseSubscriber;

public interface BoundedPromiseSubscriber<V> extends PromiseSubscriber<V> {
    void omit();
}
