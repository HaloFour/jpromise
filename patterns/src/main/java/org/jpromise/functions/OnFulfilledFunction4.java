package org.jpromise.functions;

import org.jpromise.patterns.Pattern4;

public abstract class OnFulfilledFunction4<V1, V2, V3, V4, VR> implements OnFulfilledFunction<Pattern4<V1, V2, V3, V4>, VR> {
    @Override
    public final VR fulfilled(Pattern4<V1, V2, V3, V4> result) throws Throwable {
        if (result == null) {
            return fulfilled(null, null, null, null);
        }
        else {
            return fulfilled(result.item1, result.item2, result.item3, result.item4);
        }
    }

    public abstract VR fulfilled(V1 item1, V2 item2, V3 item3, V4 item4) throws Throwable;
}
