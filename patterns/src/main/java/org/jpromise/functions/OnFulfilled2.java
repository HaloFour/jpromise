package org.jpromise.functions;

import org.jpromise.patterns.Pattern2;

public abstract class OnFulfilled2<V1, V2> implements OnFulfilled<Pattern2<V1, V2>> {
    @Override
    public final void fulfilled(Pattern2<V1, V2> result) throws Throwable {
        if (result == null) {
            fulfilled(null, null);
        }
        else {
            fulfilled(result.item1, result.item2);
        }
    }

    public abstract void fulfilled(V1 item1, V2 item2) throws Throwable;
}
