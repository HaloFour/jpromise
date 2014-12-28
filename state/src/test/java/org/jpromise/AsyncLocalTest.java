package org.jpromise;

import org.jpromise.functions.OnFulfilled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AsyncLocalTest {
    @Test
    public void stateFollowsThread() throws Throwable {
        final AsyncLocal<String> local = new AsyncLocal<String>();
        local.set("SUCCESS1");
        final Thread thread = Thread.currentThread();

        Promise<String> promise = Promises.fulfilled("SUCCESS1");
        promise.then(PromiseExecutors.NEW_THREAD, new OnFulfilled<String>() {
            @Override
            public void fulfilled(String result) throws Throwable {
                assertNotEquals(thread, Thread.currentThread());
                String value = local.get();
                assertEquals("SUCCESS1", value);
            }
        });

        PromiseHelpers.assertFulfills(promise);
    }

    @Test
    public void asyncLocalCleanUp() throws Throwable {
        AsyncLocal.list.clear();
        AsyncLocal<String> local = new AsyncLocal<String>();
        assertEquals(1, AsyncLocal.list.size());
        local.finalize();
        assertEquals(0, AsyncLocal.list.size());
    }
}
