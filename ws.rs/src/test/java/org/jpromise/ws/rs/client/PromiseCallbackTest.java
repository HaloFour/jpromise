package org.jpromise.ws.rs.client;

import org.jpromise.Promise;
import org.junit.Test;

import static org.jpromise.PromiseHelpers.*;

public class PromiseCallbackTest {
    private static final String SUCCESS1 = "SUCCESS1";
    private static final Throwable EXCEPTION = new Throwable();

    @Test
    public void completed() throws Throwable {
        PromiseCallback<String> callback = new PromiseCallback<String>() { };
        Promise<String> promise = callback.promise;

        callback.completed(SUCCESS1);

        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void failed() throws Throwable {
        PromiseCallback<String> callback = new PromiseCallback<String>() { };
        Promise<String> promise = callback.promise;

        callback.failed(EXCEPTION);

        assertRejects(EXCEPTION, promise);
    }
}
