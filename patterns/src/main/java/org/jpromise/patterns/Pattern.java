package org.jpromise.patterns;

import org.jpromise.Promise;
import org.jpromise.PromiseManager;
import org.jpromise.functions.*;
import org.jpromise.functions.OnResolved2;
import org.jpromise.functions.OnResolved3;
import org.jpromise.functions.OnResolved4;
import org.jpromise.functions.OnResolved5;
import org.jpromise.functions.OnResolvedFunction2;
import org.jpromise.functions.OnResolvedFunction3;
import org.jpromise.functions.OnResolvedFunction4;
import org.jpromise.functions.OnResolvedFunction5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Pattern {
    private Pattern() { }

    public static <V1, V2> Promise<Pattern2<V1, V2>> join(final Promise<V1> first, final Promise<V2> second) {
        List<Promise<?>> promises = new ArrayList<Promise<?>>(2);
        promises.add(first);
        promises.add(second);
        return PromiseManager.whenAllResolved(promises)
                .thenApply(new OnResolvedFunction<Void, Pattern2<V1, V2>>() {
                    @Override
                    public Pattern2<V1, V2> resolved(Void result) throws Throwable {
                        return new Pattern2<V1, V2>(getResult(first), getResult(second));
                    }
                });
    }

    public static <V1, V2, V3> Promise<Pattern3<V1, V2, V3>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third) {
        List<Promise<?>> promises = new ArrayList<Promise<?>>(3);
        promises.add(first);
        promises.add(second);
        promises.add(third);
        return PromiseManager.whenAllResolved(promises)
                .thenApply(new OnResolvedFunction<Void, Pattern3<V1, V2, V3>>() {
                    @Override
                    public Pattern3<V1, V2, V3> resolved(Void result) throws Throwable {
                        return new Pattern3<V1, V2, V3>(getResult(first), getResult(second), getResult(third));
                    }
                });
    }

    public static <V1, V2, V3, V4> Promise<Pattern4<V1, V2, V3, V4>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third, final Promise<V4> fourth) {
        List<Promise<?>> promises = new ArrayList<Promise<?>>(4);
        promises.add(first);
        promises.add(second);
        promises.add(third);
        promises.add(fourth);
        return PromiseManager.whenAllResolved(promises)
                .thenApply(new OnResolvedFunction<Void, Pattern4<V1, V2, V3, V4>>() {
                    @Override
                    public Pattern4<V1, V2, V3, V4> resolved(Void result) throws Throwable {
                        return new Pattern4<V1, V2, V3, V4>(getResult(first), getResult(second), getResult(third), getResult(fourth));
                    }
                });
    }

    public static <V1, V2, V3, V4, V5> Promise<Pattern5<V1, V2, V3, V4, V5>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third, final Promise<V4> fourth, final Promise<V5> fifth) {
        List<Promise<?>> promises = new ArrayList<Promise<?>>(4);
        promises.add(first);
        promises.add(second);
        promises.add(third);
        promises.add(fourth);
        promises.add(fifth);
        return PromiseManager.whenAllResolved(promises)
                .thenApply(new OnResolvedFunction<Void, Pattern5<V1, V2, V3, V4, V5>>() {
                    @Override
                    public Pattern5<V1, V2, V3, V4, V5> resolved(Void result) throws Throwable {
                        return new Pattern5<V1, V2, V3, V4, V5>(getResult(first), getResult(second), getResult(third), getResult(fourth), getResult(fifth));
                    }
                });
    }

    private static <V> V getResult(Promise<V> promise) throws InterruptedException, ExecutionException {
        if (promise == null) {
            return null;
        }
        return promise.get();
    }

    public static <V1, V2> OnResolved<Pattern2<V1, V2>> then(OnResolved2<V1, V2> action) {
        return action;
    }

    public static <V1, V2, V3> OnResolved<Pattern3<V1, V2, V3>> then(OnResolved3<V1, V2, V3> action) {
        return action;
    }

    public static <V1, V2, V3, V4> OnResolved<Pattern4<V1, V2, V3, V4>> then(OnResolved4<V1, V2, V3, V4> action) {
        return action;
    }

    public static <V1, V2, V3, V4, V5> OnResolved<Pattern5<V1, V2, V3, V4, V5>> then(OnResolved5<V1, V2, V3, V4, V5> action) {
        return action;
    }

    public static <V1, V2, VR> OnResolvedFunction<Pattern2<V1, V2>, VR> apply(OnResolvedFunction2<V1, V2, VR> function) {
        return function;
    }

    public static <V1, V2, V3, VR> OnResolvedFunction<Pattern3<V1, V2, V3>, VR> apply(OnResolvedFunction3<V1, V2, V3, VR> function) {
        return function;
    }

    public static <V1, V2, V3, V4, VR> OnResolvedFunction<Pattern4<V1, V2, V3, V4>, VR> apply(OnResolvedFunction4<V1, V2, V3, V4, VR> function) {
        return function;
    }

    public static <V1, V2, V3, V4, V5, VR> OnResolvedFunction<Pattern5<V1, V2, V3, V4, V5>, VR> apply(OnResolvedFunction5<V1, V2, V3, V4, V5, VR> function) {
        return function;
    }

    public static <V1, V2, VR> OnResolvedFunction<Pattern2<V1, V2>, ? extends Future<VR>> compose(OnResolvedFunction2<V1, V2, ? extends Future<VR>> function) {
        return function;
    }

    public static <V1, V2, V3, VR> OnResolvedFunction<Pattern3<V1, V2, V3>, ? extends Future<VR>> compose(OnResolvedFunction3<V1, V2, V3, ? extends Future<VR>> function) {
        return function;
    }

    public static <V1, V2, V3, V4, VR> OnResolvedFunction<Pattern4<V1, V2, V3, V4>, ? extends Future<VR>> compose(OnResolvedFunction4<V1, V2, V3, V4, ? extends Future<VR>> function) {
        return function;
    }

    public static <V1, V2, V3, V4, V5, VR> OnResolvedFunction<Pattern5<V1, V2, V3, V4, V5>, ? extends Future<VR>> compose(OnResolvedFunction5<V1, V2, V3, V4, V5, ? extends Future<VR>> function) {
        return function;
    }
}
