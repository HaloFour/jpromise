package org.jpromise.functions;

import org.jpromise.patterns.Pattern3;

public abstract class OnFulfilledFunction3<V1, V2, V3, VR> implements OnFulfilledFunction<Pattern3<V1, V2, V3>, VR> {
    @Override
    public final VR fulfilled(Pattern3<V1, V2, V3> result) throws Throwable {
        if (result == null) {
            return fulfilled(null, null, null);
        }
        else {
            return fulfilled(result.item1, result.item2, result.item3);
        }
    }

    public abstract VR fulfilled(V1 item1, V2 item2, V3 item3) throws Throwable;
}
