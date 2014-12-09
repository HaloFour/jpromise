package org.jpromise;

public interface PromiseCallbackListener {
    AutoCloseable invokingPromiseCallback(Promise<?> source, Promise<?> target, Object result, Throwable exception);
}
