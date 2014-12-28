package org.jpromise;

import org.junit.Test;

import static org.jpromise.PromiseHelpers.*;
import static org.junit.Assert.*;

public class DeferredTest {
    public final String SUCCESS1 = "SUCCESS1";

    @Test
    public void fulfill() throws Exception {
        Deferred<String> deferred = Promises.defer();
        Promise<String> promise = deferred.promise();
        assertFalse(promise.isDone());
        assertTrue(deferred.fulfill(SUCCESS1));
        assertFulfills(SUCCESS1, promise);
    }

    @Test
    public void reject() throws Exception {
        Deferred<String> deferred = Promises.defer();
        Promise<String> promise = deferred.promise();
        Exception exception = new Exception();
        assertFalse(promise.isDone());
        assertTrue(deferred.reject(exception));
        assertRejects(exception, promise);
    }

    @Test
    public void fulfillAlreadyCompletedReturnsFalse() throws Exception {
        Deferred<String> deferred = Promises.defer();
        deferred.fulfill(SUCCESS1);
        assertFalse(deferred.fulfill(SUCCESS1));
    }

    @Test
    public void rejectAlreadyCompletedReturnsFalse() throws Exception {
        Deferred<String> deferred = Promises.defer();
        deferred.fulfill(SUCCESS1);
        Exception exception = new Exception();
        assertFalse(deferred.reject(exception));
    }

}
