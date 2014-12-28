package org.jpromise.functions;

import org.jpromise.patterns.Pattern5;

public abstract class OnFulfilledFunction5<V1, V2, V3, V4, V5, VR> implements OnFulfilledFunction<Pattern5<V1, V2, V3, V4, V5>, VR> {
    @Override
    public final VR fulfilled(Pattern5<V1, V2, V3, V4, V5> result) throws Throwable {
        if (result == null) {
            return fulfilled(null, null, null, null, null);
        }
        else {
            return fulfilled(result.item1, result.item2, result.item3, result.item4, result.item5);
        }
    }

    public abstract VR fulfilled(V1 item1, V2 item2, V3 item3, V4 item4, V5 item5) throws Throwable;
}
