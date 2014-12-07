package org.jpromise.functions;

import org.jpromise.patterns.Pattern2;

public abstract class OnResolvedFunction2<V1, V2, VR> implements OnResolvedFunction<Pattern2<V1, V2>, VR> {
    @Override
    public final VR resolved(Pattern2<V1, V2> result) throws Throwable {
        if (result == null) {
            return resolved(null, null);
        }
        else {
            return resolved(result.item1, result.item2);
        }
    }

    public abstract VR resolved(V1 item1, V2 item2) throws Throwable;
}
