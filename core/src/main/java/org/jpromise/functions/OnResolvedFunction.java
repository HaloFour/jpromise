package org.jpromise.functions;

public interface OnResolvedFunction<V1, V2> {
    V2 resolved(V1 result) throws Throwable;
}
