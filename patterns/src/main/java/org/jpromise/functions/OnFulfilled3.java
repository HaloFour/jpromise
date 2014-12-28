package org.jpromise.functions;

import org.jpromise.patterns.Pattern3;

public abstract class OnFulfilled3<V1, V2, V3> implements OnFulfilled<Pattern3<V1, V2, V3>> {
    @Override
    public final void fulfilled(Pattern3<V1, V2, V3> result) throws Throwable {
        if (result == null) {
            fulfilled(null, null, null);
        }
        else {
            fulfilled(result.item1, result.item2, result.item3);
        }
    }

    public abstract void fulfilled(V1 item1, V2 item2, V3 item3) throws Throwable;
}
