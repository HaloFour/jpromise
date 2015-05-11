package org.jpromise;

import java.util.concurrent.Future;

public interface FuturePromiseAdapter {
    <R> Promise<R> adapt(Future<R> future);
}
