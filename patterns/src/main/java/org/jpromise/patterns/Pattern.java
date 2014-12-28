package org.jpromise.patterns;

import org.jpromise.Promise;
import org.jpromise.PromiseManager;
import org.jpromise.functions.*;
import org.jpromise.functions.OnFulfilled2;
import org.jpromise.functions.OnFulfilled3;
import org.jpromise.functions.OnFulfilled4;
import org.jpromise.functions.OnFulfilled5;
import org.jpromise.functions.OnFulfilledFunction2;
import org.jpromise.functions.OnFulfilledFunction3;
import org.jpromise.functions.OnFulfilledFunction4;
import org.jpromise.functions.OnFulfilledFunction5;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Utility methods for joining multiple asynchronous operations into a single operation
 * and decomposing the results into separate arguments.
 * @see org.jpromise.Promise
 */
public class Pattern {
    private Pattern() {
        throw new IllegalStateException();
    }

    /**
     * Creates a pattern from the two specified values.
     * @param first The first value.
     * @param second The second value.
     * @param <V1> The type of the first value.
     * @param <V2> The type of the second value.
     * @return A {@link org.jpromise.patterns.Pattern2} of the two values.
     */
    public static <V1, V2> Pattern2<V1, V2> of(V1 first, V2 second) {
        return new Pattern2<V1, V2>(first, second);
    }

    /**
     * Creates a pattern from the three specified values.
     * @param first The first value.
     * @param second The second value.
     * @param third The third value.
     * @param <V1> The type of the first value.
     * @param <V2> The type of the second value.
     * @param <V3> The type of the third value.
     * @return A {@link org.jpromise.patterns.Pattern3} of the three values.
     */
    public static <V1, V2, V3> Pattern3<V1, V2, V3> of(V1 first, V2 second, V3 third) {
        return new Pattern3<V1, V2, V3>(first, second, third);
    }

    /**
     * Creates a pattern from the four specified values.
     * @param first The first value.
     * @param second The second value.
     * @param third The third value.
     * @param fourth The fourth value.
     * @param <V1> The type of the first value.
     * @param <V2> The type of the second value.
     * @param <V3> The type of the third value.
     * @param <V4> The type of the fourth value.
     * @return A {@link org.jpromise.patterns.Pattern4} of the four values.
     */
    public static <V1, V2, V3, V4> Pattern4<V1, V2, V3, V4> of(V1 first, V2 second, V3 third, V4 fourth) {
        return new Pattern4<V1, V2, V3, V4>(first, second, third, fourth);
    }

    /**
     * Creates a pattern from the five specified values.
     * @param first The first value.
     * @param second The second value.
     * @param third The third value.
     * @param fourth The fourth value.
     * @param fifth The fifth value.
     * @param <V1> The type of the first value.
     * @param <V2> The type of the second value.
     * @param <V3> The type of the third value.
     * @param <V4> The type of the fourth value.
     * @param <V5> The type of the fifth value.
     * @return A {@link org.jpromise.patterns.Pattern5} of the five values.
     */
    public static <V1, V2, V3, V4, V5> Pattern5<V1, V2, V3, V4, V5> of(V1 first, V2 second, V3 third, V4 fourth, V5 fifth) {
        return new Pattern5<V1, V2, V3, V4, V5>(first, second, third, fourth, fifth);
    }

    /**
     * Creates a single {@link org.jpromise.Promise} representing the successful completion
     * of both of the specified promises.  If either of the promises is rejected then the
     * created promise will also be rejected with the same exception.
     * @param first The first promise.
     * @param second The second promise.
     * @param <V1> The result type of the first promise.
     * @param <V2> The result type of the second promise.
     * @return A promise that will be completed when both promises are fulfilled.
     */
    public static <V1, V2> Promise<Pattern2<V1, V2>> join(final Promise<V1> first, final Promise<V2> second) {
        return PromiseManager.whenAllFulfilled(first, second)
                .thenApply(new OnFulfilledFunction<Void, Pattern2<V1, V2>>() {
                    @Override
                    public Pattern2<V1, V2> fulfilled(Void result) throws Throwable {
                        return new Pattern2<V1, V2>(getResult(first), getResult(second));
                    }
                });
    }

    /**
     * Creates a single {@link org.jpromise.Promise} representing the successful completion
     * of all of the specified promises.  If any of the promises is rejected then the
     * created promise will also be rejected with the same exception.
     * @param first The first promise.
     * @param second The second promise.
     * @param third The third promise.
     * @param <V1> The result type of the first promise.
     * @param <V2> The result type of the second promise.
     * @param <V3> The result type of the third promise.
     * @return A promise that will be completed when all of the promises are fulfilled.
     */
    public static <V1, V2, V3> Promise<Pattern3<V1, V2, V3>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third) {
        return PromiseManager.whenAllFulfilled(first, second, third)
                .thenApply(new OnFulfilledFunction<Void, Pattern3<V1, V2, V3>>() {
                    @Override
                    public Pattern3<V1, V2, V3> fulfilled(Void result) throws Throwable {
                        return new Pattern3<V1, V2, V3>(getResult(first), getResult(second), getResult(third));
                    }
                });
    }

    /**
     * Creates a single {@link org.jpromise.Promise} representing the successful completion
     * of all of the specified promises.  If any of the promises is rejected then the
     * created promise will also be rejected with the same exception.
     * @param first The first promise.
     * @param second The second promise.
     * @param third The third promise.
     * @param fourth The fourth promise.
     * @param <V1> The result type of the first promise.
     * @param <V2> The result type of the second promise.
     * @param <V3> The result type of the third promise.
     * @param <V4> The result type of the fourth promise.
     * @return A promise that will be completed when all of the promises are fulfilled.
     */
    public static <V1, V2, V3, V4> Promise<Pattern4<V1, V2, V3, V4>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third, final Promise<V4> fourth) {
        return PromiseManager.whenAllFulfilled(first, second, third, fourth)
                .thenApply(new OnFulfilledFunction<Void, Pattern4<V1, V2, V3, V4>>() {
                    @Override
                    public Pattern4<V1, V2, V3, V4> fulfilled(Void result) throws Throwable {
                        return new Pattern4<V1, V2, V3, V4>(getResult(first), getResult(second), getResult(third), getResult(fourth));
                    }
                });
    }

    /**
     * Creates a single {@link org.jpromise.Promise} representing the successful completion
     * of all of the specified promises.  If any of the promises is rejected then the
     * created promise will also be rejected with the same exception.
     * @param first The first promise.
     * @param second The second promise.
     * @param third The third promise.
     * @param fourth The fourth promise.
     * @param fifth The fifth promise.
     * @param <V1> The result type of the first promise.
     * @param <V2> The result type of the second promise.
     * @param <V3> The result type of the third promise.
     * @param <V4> The result type of the fourth promise.
     * @param <V5> The result type of the fifth promise.
     * @return A promise that will be completed when all of the promises are fulfilled.
     */
    public static <V1, V2, V3, V4, V5> Promise<Pattern5<V1, V2, V3, V4, V5>> join(final Promise<V1> first, final Promise<V2> second, final Promise<V3> third, final Promise<V4> fourth, final Promise<V5> fifth) {
        return PromiseManager.whenAllFulfilled(first, second, third, fourth, fifth)
                .thenApply(new OnFulfilledFunction<Void, Pattern5<V1, V2, V3, V4, V5>>() {
                    @Override
                    public Pattern5<V1, V2, V3, V4, V5> fulfilled(Void result) throws Throwable {
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

    /**
     * Helper method to be combined with the {@link org.jpromise.Promise#then(org.jpromise.functions.OnFulfilled)}
     * method to decompose the separate values of the {@link org.jpromise.patterns.Pattern2} into
     * separate arguments.
     * @param action The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @return An {@link org.jpromise.functions.OnFulfilled} that can be passed as an
     * argument to the {@link org.jpromise.Promise#then(org.jpromise.functions.OnFulfilled)} method.
     */
    public static <V1, V2> OnFulfilled<Pattern2<V1, V2>> spread2(OnFulfilled2<V1, V2> action) {
        return action;
    }

    /**
     * Helper method to be combined with the {@link org.jpromise.Promise#then(org.jpromise.functions.OnFulfilled)}
     * method to decompose the separate values of the {@link org.jpromise.patterns.Pattern3} into
     * separate arguments.
     * @param action The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @return An {@link org.jpromise.functions.OnFulfilled} that can be passed as an
     * argument to the {@link org.jpromise.Promise#then(org.jpromise.functions.OnFulfilled)} method.
     */
    public static <V1, V2, V3> OnFulfilled<Pattern3<V1, V2, V3>> spread3(OnFulfilled3<V1, V2, V3> action) {
        return action;
    }

    /**
     * Helper method to be combined with the {@link org.jpromise.Promise#then(org.jpromise.functions.OnFulfilled)}
     * method to decompose the separate values of the {@link org.jpromise.patterns.Pattern4} into
     * separate arguments.
     * @param action The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @param <V4> The type of the fourth argument.
     * @return An {@link org.jpromise.functions.OnFulfilled} that can be passed as an
     * argument to the {@link org.jpromise.Promise#then(org.jpromise.functions.OnFulfilled)} method.
     */
    public static <V1, V2, V3, V4> OnFulfilled<Pattern4<V1, V2, V3, V4>> spread4(OnFulfilled4<V1, V2, V3, V4> action) {
        return action;
    }

    /**
     * Helper method to be combined with the {@link org.jpromise.Promise#then(org.jpromise.functions.OnFulfilled)}
     * method to decompose the separate values of the {@link org.jpromise.patterns.Pattern5} into
     * separate arguments.
     * @param action The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @param <V4> The type of the fourth argument.
     * @param <V5> The type of the fifth argument.
     * @return An {@link org.jpromise.functions.OnFulfilled} that can be passed as an
     * argument to the {@link org.jpromise.Promise#then(org.jpromise.functions.OnFulfilled)} method.
     */
    public static <V1, V2, V3, V4, V5> OnFulfilled<Pattern5<V1, V2, V3, V4, V5>> spread5(OnFulfilled5<V1, V2, V3, V4, V5> action) {
        return action;
    }

    /**
     * Helper method to be combined with the
     * {@link org.jpromise.Promise#thenApply(org.jpromise.functions.OnFulfilledFunction)} method to decompose
     * the separate values of the {@link org.jpromise.patterns.Pattern2} into separate arguments.
     * @param function The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @return An {@link org.jpromise.functions.OnFulfilledFunction} that can be passed as an
     * argument to the {@link org.jpromise.Promise#thenApply(org.jpromise.functions.OnFulfilledFunction)} method.
     */
    public static <V1, V2, VR> OnFulfilledFunction<Pattern2<V1, V2>, VR> apply2(OnFulfilledFunction2<V1, V2, VR> function) {
        return function;
    }

    /**
     * Helper method to be combined with the
     * {@link org.jpromise.Promise#thenApply(org.jpromise.functions.OnFulfilledFunction)} method to decompose
     * the separate values of the {@link org.jpromise.patterns.Pattern3} into separate arguments.
     * @param function The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @return An {@link org.jpromise.functions.OnFulfilledFunction} that can be passed as an
     * argument to the {@link org.jpromise.Promise#thenApply(org.jpromise.functions.OnFulfilledFunction)} method.
     */
    public static <V1, V2, V3, VR> OnFulfilledFunction<Pattern3<V1, V2, V3>, VR> apply3(OnFulfilledFunction3<V1, V2, V3, VR> function) {
        return function;
    }

    /**
     * Helper method to be combined with the
     * {@link org.jpromise.Promise#thenApply(org.jpromise.functions.OnFulfilledFunction)} method to decompose
     * the separate values of the {@link org.jpromise.patterns.Pattern4} into separate arguments.
     * @param function The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @param <V4> The type of the fourth argument.
     * @return An {@link org.jpromise.functions.OnFulfilledFunction} that can be passed as an
     * argument to the {@link org.jpromise.Promise#thenApply(org.jpromise.functions.OnFulfilledFunction)} method.
     */
    public static <V1, V2, V3, V4, VR> OnFulfilledFunction<Pattern4<V1, V2, V3, V4>, VR> apply4(OnFulfilledFunction4<V1, V2, V3, V4, VR> function) {
        return function;
    }

    /**
     * Helper method to be combined with the
     * {@link org.jpromise.Promise#thenApply(org.jpromise.functions.OnFulfilledFunction)} method to decompose
     * the separate values of the {@link org.jpromise.patterns.Pattern5} into separate arguments.
     * @param function The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @param <V4> The type of the fourth argument.
     * @param <V5> The type of the fifth argument.
     * @return An {@link org.jpromise.functions.OnFulfilledFunction} that can be passed as an
     * argument to the {@link org.jpromise.Promise#thenApply(org.jpromise.functions.OnFulfilledFunction)} method.
     */
    public static <V1, V2, V3, V4, V5, VR> OnFulfilledFunction<Pattern5<V1, V2, V3, V4, V5>, VR> apply5(OnFulfilledFunction5<V1, V2, V3, V4, V5, VR> function) {
        return function;
    }

    /**
     * Helper method to be combined with the
     * {@link org.jpromise.Promise#thenCompose(org.jpromise.functions.OnFulfilledFunction)} method to decompose
     * the separate values of the {@link org.jpromise.patterns.Pattern2} into separate arguments.
     * @param function The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @return An {@link org.jpromise.functions.OnFulfilledFunction} that can be passed as an
     * argument to the {@link org.jpromise.Promise#thenCompose(org.jpromise.functions.OnFulfilledFunction)} method.
     */
    public static <V1, V2, VR> OnFulfilledFunction<Pattern2<V1, V2>, ? extends Future<VR>> compose2(OnFulfilledFunction2<V1, V2, ? extends Future<VR>> function) {
        return function;
    }

    /**
     * Helper method to be combined with the
     * {@link org.jpromise.Promise#thenCompose(org.jpromise.functions.OnFulfilledFunction)} method to decompose
     * the separate values of the {@link org.jpromise.patterns.Pattern3} into separate arguments.
     * @param function The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @return An {@link org.jpromise.functions.OnFulfilledFunction} that can be passed as an
     * argument to the {@link org.jpromise.Promise#thenCompose(org.jpromise.functions.OnFulfilledFunction)} method.
     */
    public static <V1, V2, V3, VR> OnFulfilledFunction<Pattern3<V1, V2, V3>, ? extends Future<VR>> compose3(OnFulfilledFunction3<V1, V2, V3, ? extends Future<VR>> function) {
        return function;
    }

    /**
     * Helper method to be combined with the
     * {@link org.jpromise.Promise#thenCompose(org.jpromise.functions.OnFulfilledFunction)} method to decompose
     * the separate values of the {@link org.jpromise.patterns.Pattern4} into separate arguments.
     * @param function The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @param <V4> The type of the fourth argument.
     * @return An {@link org.jpromise.functions.OnFulfilledFunction} that can be passed as an
     * argument to the {@link org.jpromise.Promise#thenCompose(org.jpromise.functions.OnFulfilledFunction)} method.
     */
    public static <V1, V2, V3, V4, VR> OnFulfilledFunction<Pattern4<V1, V2, V3, V4>, ? extends Future<VR>> compose4(OnFulfilledFunction4<V1, V2, V3, V4, ? extends Future<VR>> function) {
        return function;
    }

    /**
     * Helper method to be combined with the
     * {@link org.jpromise.Promise#thenCompose(org.jpromise.functions.OnFulfilledFunction)} method to decompose
     * the separate values of the {@link org.jpromise.patterns.Pattern5} into separate arguments.
     * @param function The operation that accepts the decomposed arguments.
     * @param <V1> The type of the first argument.
     * @param <V2> The type of the second argument.
     * @param <V3> The type of the third argument.
     * @param <V4> The type of the fourth argument.
     * @param <V5> The type of the fifth argument.
     * @return An {@link org.jpromise.functions.OnFulfilledFunction} that can be passed as an
     * argument to the {@link org.jpromise.Promise#thenCompose(org.jpromise.functions.OnFulfilledFunction)} method.
     */
    public static <V1, V2, V3, V4, V5, VR> OnFulfilledFunction<Pattern5<V1, V2, V3, V4, V5>, ? extends Future<VR>> compose5(OnFulfilledFunction5<V1, V2, V3, V4, V5, ? extends Future<VR>> function) {
        return function;
    }
}
