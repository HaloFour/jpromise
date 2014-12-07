package org.jpromise.functions;

import org.jpromise.patterns.Pattern2;

public abstract class OnResolved2<V1, V2> implements OnResolved<Pattern2<V1, V2>> {
    @Override
    public final void resolved(Pattern2<V1, V2> result) throws Throwable {
        if (result == null) {
            resolved(null, null);
        }
        else {
            resolved(result.item1, result.item2);
        }
    }

    public abstract void resolved(V1 item1, V2 item2) throws Throwable;
}
