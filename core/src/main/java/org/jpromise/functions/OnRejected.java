package org.jpromise.functions;

public interface OnRejected<E extends Throwable> {
    void rejected(E exception) throws Throwable;
}
