package org.jpromise.functions;

import org.jpromise.patterns.Pattern4;

public abstract class OnFulfilled4<V1, V2, V3, V4> implements OnFulfilled<Pattern4<V1, V2, V3, V4>> {
    @Override
    public final void fulfilled(Pattern4<V1, V2, V3, V4> result) throws Throwable {
        if (result == null) {
            fulfilled(null, null, null, null);
        }
        else {
            fulfilled(result.item1, result.item2, result.item3, result.item4);
        }
    }

    public abstract void fulfilled(V1 item1, V2 item2, V3 item3, V4 item4) throws Throwable;
}
