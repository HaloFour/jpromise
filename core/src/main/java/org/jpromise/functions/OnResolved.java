package org.jpromise.functions;

public interface OnResolved<V> {
    void resolved(V result) throws Throwable;
}
