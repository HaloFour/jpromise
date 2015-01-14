package org.jpromise.functions;

import java.util.concurrent.Future;

public interface FutureGenerator<V> {
    Future<V> next(V previous);
}
