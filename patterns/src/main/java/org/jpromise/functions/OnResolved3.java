package org.jpromise.functions;

import org.jpromise.patterns.Pattern3;

public abstract class OnResolved3<V1, V2, V3> implements OnResolved<Pattern3<V1, V2, V3>> {
    @Override
    public final void resolved(Pattern3<V1, V2, V3> result) throws Throwable {
        if (result == null) {
            resolved(null, null, null);
        }
        else {
            resolved(result.item1, result.item2, result.item3);
        }
    }

    public abstract void resolved(V1 item1, V2 item2, V3 item3) throws Throwable;
}
