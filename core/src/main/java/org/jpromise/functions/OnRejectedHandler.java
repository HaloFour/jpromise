package org.jpromise.functions;

public interface OnRejectedHandler<E extends Throwable, V> {
    V handle(E exception) throws Throwable;
}
