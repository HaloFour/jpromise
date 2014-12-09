package org.jpromise.slf4j;

import org.jpromise.Promise;
import org.jpromise.PromiseComposition;
import org.jpromise.PromiseExecutors;
import org.jpromise.functions.OnResolved;
import org.junit.Test;
import org.slf4j.MDC;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;

public class AsyncContextTest {
    private static final String SUCCESS1 = "SUCCESS1";

    private static final String KEY1 = "KEY1";
    private static final String VALUE1 = "VALUE1";
    private static final String VALUE2 = "VALUE2";

    @Test
    public void testMdc() throws Throwable {
        PromiseComposition.clear();
        PromiseComposition.register(new MDCPromiseTracker());
        MDC.put(KEY1, VALUE1);
        final Thread thread = Thread.currentThread();

        Promise<String> promise1 = Promise.resolved(SUCCESS1);

        Promise<String> promise2 = promise1.then(PromiseExecutors.NEW_THREAD, new OnResolved<String>() {
            @Override
            public void resolved(String result) throws Throwable {
                assertNotEquals(thread, Thread.currentThread());
                String value = MDC.get(KEY1);
                assertEquals(VALUE1, value);
            }
        });

        assertResolves(SUCCESS1, promise2);
    }
}
