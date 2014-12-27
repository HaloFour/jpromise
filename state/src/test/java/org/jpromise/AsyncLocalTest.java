package org.jpromise;

import org.jpromise.functions.OnResolved;
import org.junit.Test;

import static org.jpromise.PromiseHelpers.assertResolves;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AsyncLocalTest {
    @Test
    public void stateFollowsThread() throws Throwable {
        final AsyncLocal<String> local = new AsyncLocal<String>();
        local.set("SUCCESS1");
        final Thread thread = Thread.currentThread();

        Promise<String> promise = Promises.resolved("SUCCESS1");
        promise.then(PromiseExecutors.NEW_THREAD, new OnResolved<String>() {
            @Override
            public void resolved(String result) throws Throwable {
                assertNotEquals(thread, Thread.currentThread());
                String value = local.get();
                assertEquals("SUCCESS1", value);
            }
        });

        assertResolves(promise);
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
