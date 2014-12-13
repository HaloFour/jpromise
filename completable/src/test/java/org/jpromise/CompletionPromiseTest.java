package org.jpromise;

import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.assertEquals;

public class CompletionPromiseTest {
    private static final String SUCCESS1 = "SUCCESS1";

    @Test
    public void completionPromiseResolves() throws Throwable {
        CompletableFuture<String> future = new CompletableFuture<>();
        CompletionPromise<String> promise = new CompletionPromise<>(future);

        future.complete(SUCCESS1);

        assertResolves(SUCCESS1, promise);
    }

    @Test
    public void completionPromiseRejects() throws Throwable {
        Throwable exception = new Throwable();
        CompletableFuture<String> future = new CompletableFuture<>();
        CompletionPromise<String> promise = new CompletionPromise<>(future);

        future.completeExceptionally(exception);

        assertRejects(exception, promise);
    }

    @Test
    public void completionStageCompletes() throws Throwable {
        Promise<String> promise = resolveAfter(SUCCESS1, 10);
        CompletionStage<String> completionStage = CompletionPromise.toCompletionStage(promise);
        CompletableFuture<String> future = completionStage.toCompletableFuture();

        String result = future.get();

        assertEquals(SUCCESS1, result);
    }

    @Test
    public void completionStageCompletesExceptionally() throws Throwable {
        Throwable exception = new Throwable();
        Promise<String> promise = rejectAfter(exception, 10);
        CompletionStage<String> completionStage = CompletionPromise.toCompletionStage(promise);
        CompletableFuture<String> future = completionStage.toCompletableFuture();

        try {
            String ignored = future.get();
            throw new AssertionFailedError("future.get() should not complete successfully");
        }
        catch (ExecutionException executionException) {
            assertEquals(exception, executionException.getCause());
        }
    }
}
