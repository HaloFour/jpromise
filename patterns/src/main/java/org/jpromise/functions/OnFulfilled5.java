package org.jpromise.functions;

import org.jpromise.patterns.Pattern5;

public abstract class OnFulfilled5<V1, V2, V3, V4, V5> implements OnFulfilled<Pattern5<V1, V2, V3, V4, V5>> {
    @Override
    public final void fulfilled(Pattern5<V1, V2, V3, V4, V5> result) throws Throwable {
        if (result == null) {
            fulfilled(null, null, null, null, null);
        }
        else {
            fulfilled(result.item1, result.item2, result.item3, result.item4, result.item5);
        }
    }

    public abstract void fulfilled(V1 item1, V2 item2, V3 item3, V4 item4, V5 item5) throws Throwable;
}
