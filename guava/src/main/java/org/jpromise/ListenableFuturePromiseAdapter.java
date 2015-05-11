package org.jpromise;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Future;

public class ListenableFuturePromiseAdapter implements FuturePromiseAdapter {
    @Override
    public <R> Promise<R> adapt(Future<R> future) {
        if (future instanceof ListenableFuture) {
            return new ListenablePromise<R>((ListenableFuture<R>)future);
        }
        return null;
    }
}
