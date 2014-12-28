package org.jpromise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class CompletionPromise<V> extends AbstractPromise<V> {
    public CompletionPromise(final CompletionStage<V> completionStage) {
        if (completionStage == null) throw new IllegalArgumentException(mustNotBeNull("completionStage"));
        completionStage.whenCompleteAsync((result, exception) -> {
            if (exception != null) {
                completeWithException(exception);
            }
            else {
                complete(result);
            }
        });
    }

    public static <V> CompletionStage<V> toCompletionStage(Promise<V> promise) {
        if (promise == null) throw new IllegalArgumentException(mustNotBeNull("promise"));
        CompletableFuture<V> future = new CompletableFuture<>();
        promise.whenCompleted((p, result, exception) -> {
            switch (p.state()) {
                case FULFILLED:
                    future.complete(result);
                    break;
                case REJECTED:
                    future.completeExceptionally(exception);
                    break;
            }
        });
        return future;
    }
}
