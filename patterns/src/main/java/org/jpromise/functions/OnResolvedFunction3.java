package org.jpromise.functions;

import org.jpromise.patterns.Pattern3;

public abstract class OnResolvedFunction3<V1, V2, V3, VR> implements OnResolvedFunction<Pattern3<V1, V2, V3>, VR> {
    @Override
    public final VR resolved(Pattern3<V1, V2, V3> result) throws Throwable {
        if (result == null) {
            return resolved(null, null, null);
        }
        else {
            return resolved(result.item1, result.item2, result.item3);
        }
    }

    public abstract VR resolved(V1 item1, V2 item2, V3 item3) throws Throwable;
}
