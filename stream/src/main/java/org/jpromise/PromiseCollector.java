package org.jpromise;

public interface PromiseCollector<V, A, R> {
    A getAccumulator() throws Throwable;
    void accumulate(A accumulator, V result) throws Throwable;
    R finish(A accumulator);
}
