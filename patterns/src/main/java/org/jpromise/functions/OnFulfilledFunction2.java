package org.jpromise.functions;

import org.jpromise.patterns.Pattern2;

public abstract class OnFulfilledFunction2<V1, V2, VR> implements OnFulfilledFunction<Pattern2<V1, V2>, VR> {
    @Override
    public final VR fulfilled(Pattern2<V1, V2> result) throws Throwable {
        if (result == null) {
            return fulfilled(null, null);
        }
        else {
            return fulfilled(result.item1, result.item2);
        }
    }

    public abstract VR fulfilled(V1 item1, V2 item2) throws Throwable;
}
