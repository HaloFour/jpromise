package org.jpromise;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.jpromise.PromiseHelpers.assertFulfills;
import static org.jpromise.PromiseHelpers.assertRejects;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GuavaTest {
    private static final String SUCCESS1 = "SUCCESS1";

    @Test
    public void listenableFutureToPromise() throws Throwable {
        SettableFuture<String> future = SettableFuture.create();

        Promise<String> promise = Promises.fromFuture(future);

        assertTrue(promise instanceof ListenablePromise);

        future.set(SUCCESS1);

        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void listenableFutureToPromiseRejects() throws Throwable {
        SettableFuture<String> future = SettableFuture.create();

        Promise<String> promise = Promises.fromFuture(future);

        assertTrue(promise instanceof ListenablePromise);

        Throwable throwable = new Exception();
        future.setException(throwable);

        assertRejects(throwable, promise);
    }
}
