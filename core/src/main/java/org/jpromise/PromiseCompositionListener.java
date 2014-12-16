package org.jpromise;

public interface PromiseCompositionListener {
    PromiseCallbackListener composingCallback(Promise<?> source, Promise<?> target);
}
