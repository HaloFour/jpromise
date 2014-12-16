package org.jpromise;

public interface PromiseCallbackCompletion {
    void completed(Promise<?> source, Promise<?> target, Object result, Throwable exception);
}
