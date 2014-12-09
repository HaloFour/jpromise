package org.jpromise.functions;

import org.jpromise.Promise;

public interface OnPromiseCallback {
    AutoCloseable executingCallback(Promise<?> composing, Promise<?> composed, Object result, Throwable exception);
}
