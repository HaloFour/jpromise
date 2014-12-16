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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Pattern {
    private Pattern() {
        throw new IllegalStateException();
    }

    public static <V1, V2> Pattern2<V1, V2> of(V1 first, V2 second) {
        return new Pattern2<V1, V2>(first, second);
    }

    public static <V1, V2, V3> Pattern3<V1, V2, V3> of(V1 first, V2 second, V3 third) {
        return new Pattern3<V1, V2, V3>(first, second, third);
    }

    public static <V1, V2, V3, V4> Pattern4<V1, V2, V3, V4> of(V1 first, V2 second, V3 third, V4 fourth) {
        return new Pattern4<V1, V2, V3, V4>(first, second, third, fourth);
    }

    public static <V1, V2, V3, V4, V5> Pattern5<V1, V2, V3, V4, V5> of(V1 first, V2 second, V3 third, V4 fourth, V5 fifth) {
        return new Pattern5<V1, V2, V3, V4, V5>(first, second, third, fourth, fifth);
    }

    public static <V1, V2> Promise<Pattern2<V1, V2>> join(final Promise<V1> first, final Promise<V2> second) {
        return PromiseManager.whenAllResolved(first, second)
                .thenApply(new OnResolvedFunction<Void, Pattern2<V1, V2>>() {
                    @Override
                    public Pattern2<V1, V2> resolved(Void result) throws Throwable {
                        return new Pattern2<V1, V2>(getResult(first), getResult(second));
                    }
                });
    }

    public static <V1, V2, V3> Promise<Pattern3<V1, V2, V3>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third) {
        return PromiseManager.whenAllResolved(first, second, third)
                .thenApply(new OnResolvedFunction<Void, Pattern3<V1, V2, V3>>() {
                    @Override
                    public Pattern3<V1, V2, V3> resolved(Void result) throws Throwable {
                        return new Pattern3<V1, V2, V3>(getResult(first), getResult(second), getResult(third));
                    }
                });
    }

    public static <V1, V2, V3, V4> Promise<Pattern4<V1, V2, V3, V4>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third, final Promise<V4> fourth) {
        return PromiseManager.whenAllResolved(first, second, third, fourth)
                .thenApply(new OnResolvedFunction<Void, Pattern4<V1, V2, V3, V4>>() {
                    @Override
                    public Pattern4<V1, V2, V3, V4> resolved(Void result) throws Throwable {
                        return new Pattern4<V1, V2, V3, V4>(getResult(first), getResult(second), getResult(third), getResult(fourth));
                    }
                });
    }

    public static <V1, V2, V3, V4, V5> Promise<Pattern5<V1, V2, V3, V4, V5>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third, final Promise<V4> fourth, final Promise<V5> fifth) {
        return PromiseManager.whenAllResolved(first, second, third, fourth, fifth)
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

    public static <V1, V2> OnResolved<Pattern2<V1, V2>> spread2(OnResolved2<V1, V2> action) {
        return action;
    }

    public static <V1, V2, V3> OnResolved<Pattern3<V1, V2, V3>> spread3(OnResolved3<V1, V2, V3> action) {
        return action;
    }

    public static <V1, V2, V3, V4> OnResolved<Pattern4<V1, V2, V3, V4>> spread4(OnResolved4<V1, V2, V3, V4> action) {
        return action;
    }

    public static <V1, V2, V3, V4, V5> OnResolved<Pattern5<V1, V2, V3, V4, V5>> spread5(OnResolved5<V1, V2, V3, V4, V5> action) {
        return action;
    }

    public static <V1, V2, VR> OnResolvedFunction<Pattern2<V1, V2>, VR> apply2(OnResolvedFunction2<V1, V2, VR> function) {
        return function;
    }

    public static <V1, V2, V3, VR> OnResolvedFunction<Pattern3<V1, V2, V3>, VR> apply3(OnResolvedFunction3<V1, V2, V3, VR> function) {
        return function;
    }

    public static <V1, V2, V3, V4, VR> OnResolvedFunction<Pattern4<V1, V2, V3, V4>, VR> apply4(OnResolvedFunction4<V1, V2, V3, V4, VR> function) {
        return function;
    }

    public static <V1, V2, V3, V4, V5, VR> OnResolvedFunction<Pattern5<V1, V2, V3, V4, V5>, VR> apply5(OnResolvedFunction5<V1, V2, V3, V4, V5, VR> function) {
        return function;
    }

    public static <V1, V2, VR> OnResolvedFunction<Pattern2<V1, V2>, ? extends Future<VR>> compose2(OnResolvedFunction2<V1, V2, ? extends Future<VR>> function) {
        return function;
    }

    public static <V1, V2, V3, VR> OnResolvedFunction<Pattern3<V1, V2, V3>, ? extends Future<VR>> compose3(OnResolvedFunction3<V1, V2, V3, ? extends Future<VR>> function) {
        return function;
    }

    public static <V1, V2, V3, V4, VR> OnResolvedFunction<Pattern4<V1, V2, V3, V4>, ? extends Future<VR>> compose4(OnResolvedFunction4<V1, V2, V3, V4, ? extends Future<VR>> function) {
        return function;
    }

    public static <V1, V2, V3, V4, V5, VR> OnResolvedFunction<Pattern5<V1, V2, V3, V4, V5>, ? extends Future<VR>> compose5(OnResolvedFunction5<V1, V2, V3, V4, V5, ? extends Future<VR>> function) {
        return function;
    }
}
