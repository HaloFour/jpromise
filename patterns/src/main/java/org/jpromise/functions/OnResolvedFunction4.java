package org.jpromise.functions;

import org.jpromise.patterns.Pattern4;

public abstract class OnResolvedFunction4<V1, V2, V3, V4, VR> implements OnResolvedFunction<Pattern4<V1, V2, V3, V4>, VR> {
    @Override
    public final VR resolved(Pattern4<V1, V2, V3, V4> result) throws Throwable {
        if (result == null) {
            return resolved(null, null, null, null);
        }
        else {
            return resolved(result.item1, result.item2, result.item3, result.item4);
        }
    }

    public abstract VR resolved(V1 item1, V2 item2, V3 item3, V4 item4) throws Throwable;
}
