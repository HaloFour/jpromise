package org.jpromise.functions;

import org.jpromise.patterns.Pattern4;

public abstract class OnResolved4<V1, V2, V3, V4> implements OnResolved<Pattern4<V1, V2, V3, V4>> {
    @Override
    public final void resolved(Pattern4<V1, V2, V3, V4> result) throws Throwable {
        if (result == null) {
            resolved(null, null, null, null);
        }
        else {
            resolved(result.item1, result.item2, result.item3, result.item4);
        }
    }

    public abstract void resolved(V1 item1, V2 item2, V3 item3, V4 item4) throws Throwable;
}
