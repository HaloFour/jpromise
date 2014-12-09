package org.jpromise.functions;

import org.jpromise.Promise;

public interface OnPromiseComposing {
    OnPromiseCallback composingCallback(Promise<?> composing, Promise<?> composed);
}
