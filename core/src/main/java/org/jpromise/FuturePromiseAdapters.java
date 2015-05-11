package org.jpromise;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Future;

class FuturePromiseAdapters {
    private static final Object lock = new Object();
    private static List<FuturePromiseAdapter> adapters;

    public static <R> Promise<R> adapt(Future<R> future) {
        if (future == null) {
            return null;
        }
        if (future instanceof Promise) {
            return (Promise<R>)future;
        }
        if (adapters == null) {
            synchronized (lock) {
                if (adapters == null) {
                    adapters = new ArrayList<FuturePromiseAdapter>();
                    ServiceLoader<FuturePromiseAdapter> loader = ServiceLoader.load(FuturePromiseAdapter.class);
                    for (FuturePromiseAdapter adapter : loader) {
                        adapters.add(adapter);
                    }
                }
            }
        }
        for (FuturePromiseAdapter adapter : adapters) {
            Promise<R> promise = adapter.adapt(future);
            if (promise != null) {
                return promise;
            }
        }
        return null;
    }
}
