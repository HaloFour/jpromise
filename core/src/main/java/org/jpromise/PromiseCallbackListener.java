package org.jpromise;

public interface PromiseCallbackListener {
    PromiseCallbackCompletion invokingPromiseCallback(Promise<?> source, Promise<?> target, Object result, Throwable exception);
}
