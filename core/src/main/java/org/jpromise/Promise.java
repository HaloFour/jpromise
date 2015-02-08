package org.jpromise;

import org.jpromise.functions.*;

import java.util.concurrent.*;

/**
 * A {@link org.jpromise.Promise} represents the asynchronous result of an operation.  Methods are provided to check if
 * the computation is complete, to register operations to be performed upon completion, to wait for its completion, and
 * to retrieve the result of the computation.  The registered operations return additional promise instances which
 * represent the completion of those operations, which can translate the results both synchronously and asynchronously.
 * The Cancellation is performed by the {@link java.util.concurrent.Future#cancel(boolean)} method. Additional methods
 * are provided to determine if the task completed normally or was cancelled. Once a computation has completed, the
 * computation cannot be cancelled.
 * @param <V> The result type of the promise.
 */
public interface Promise<V> extends Future<V> {
    /**
     * Gets the current state of the promise.
     * @see org.jpromise.PromiseState
     * @return The current state of the promise.
     */
    PromiseState state();

    /**
     * Gets whether the promise has completed.
     * @return {@code true} if the promise has completed; otherwise, {@code false}.
     */
    boolean isDone();

    /**
     * Gets whether the promise has fulfilled successfully.
     * @return {@code true} if the promise has fulfilled successfully; otherwise, {@code false}.
     */
    boolean isFulfilled();

    /**
     * Gets whether the promise has been rejected.
     * @return {@code true} if the promise has been rejected; otherwise, {@code false}.
     */
    boolean isRejected();

    /**
     * Returns the result of the promise if it has fulfilled successfully or throws an exception if the promise
     * has been rejected.  If the promise is not yet completed immediately returns the {@code defaultValue}.
     * @param defaultValue The value to return if the promise is not completed.
     * @return The result of the promise if it has fulfilled successfully.
     * @throws ExecutionException The promise has rejected.
     * @throws CancellationException The promise has been cancelled.
     */
    V getNow(V defaultValue) throws ExecutionException, CancellationException;

    /**
     * Cancels the promise after the specified timeout period.
     * @param mayInterruptIfRunning If the current promise is the result of a continuation operation specifies that
     *                              the thread on which the operation is executing can be interrupted.
     * @param timeout The maximum amount of time to wait before attempting to cancel the promise.
     * @param timeUnit The unit of time for the {@code timeout} argument.
     * @return A promise that will indicate if this promise was successfully cancelled.
     */
    Promise<V> cancelAfter(boolean mayInterruptIfRunning, long timeout, TimeUnit timeUnit);

    /**
     * Registers an operation that is to be performed when the promise is successfully fulfilled.
     * @param action The operation that is performed when the promise is successfully fulfilled accepting the result value.
     * @return A new promise that will be fulfilled with the same value when the operation has completed.
     */
    Promise<V> then(OnFulfilled<? super V> action);

    /**
     * Registers an operation that is to be performed when the promise is successfully fulfilled using the
     * specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param action The operation that is performed when the promise is successfully fulfilled accepting the result value.
     * @return A new promise that will be fulfilled with the same value when the operation has completed.
     */
    Promise<V> then(Executor executor, OnFulfilled<? super V> action);

    /**
     * Registers an operation that is to be performed when the promise is successfully fulfilled that transforms the
     * result synchronously into another value.
     * @param function The operation that is performed when the promise is successfully fulfilled accepting the result
     *                 value and transforming the result into a different value synchronously.
     * @param <V_APPLIED> The type of the transformed value.
     * @return A new promise that will be fulfilled when the operation has completed transforming the result.
     */
    <V_APPLIED> Promise<V_APPLIED> thenApply(OnFulfilledFunction<? super V, ? extends V_APPLIED> function);

    /**
     * Registers an operation that is to be performed when the promise is successfully fulfilled that transforms the
     * result synchronously into another value using the specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param function The operation that is performed when the promise is successfully fulfilled accepting the result
     *                 value and transforming the result into a different value synchronously.
     * @param <V_APPLIED> The type of the transformed value.
     * @return A new promise that will be fulfilled when the operation has completed transforming the result.
     */
    <V_APPLIED> Promise<V_APPLIED> thenApply(Executor executor, OnFulfilledFunction<? super V, ? extends V_APPLIED> function);

    /**
     * Registers an operation that is to be performed when the promise is successfully fulfilled that composed the result
     * into another value using another asynchronous operation.
     * @param function The operation that is performed when the promise is successfully fulfilled accepting the result
     *                 value and composing the result into a different value asynchronously.
     * @param <V_COMPOSED> The type of the composed value.
     * @return A new promise that will complete propagating the result of the composed operation.
     */
    <V_COMPOSED> Promise<V_COMPOSED> thenCompose(OnFulfilledFunction<? super V, ? extends Future<V_COMPOSED>> function);

    /**
     * Registers an operation that is to be performed when the promise is successfully fulfilled that composed the result
     * into another value using another asynchronous operation using the specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param function The operation that is performed when the promise is successfully fulfilled accepting the result
     *                 value and composing the result into a different value asynchronously.
     * @param <V_COMPOSED> The type of the composed value.
     * @return A new promise that will complete propagating the result of the composed operation.
     */
    <V_COMPOSED> Promise<V_COMPOSED> thenCompose(Executor executor, OnFulfilledFunction<? super V, ? extends Future<V_COMPOSED>> function);

    /**
     * Registers an operation that is to be performed when the promise is rejected.
     * @param action The operation that is performed when the promise is rejected accepting the exception that caused
     *               the rejection.
     * @return A new promise that will be rejected with the same exception when the operation has completed.
     */
    Promise<V> whenRejected(OnRejected<Throwable> action);

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param action The operation that is performed when the promise is rejected with an exception that is of or
     *               subclasses the specified {@code exceptionClass}, accepting the exception that caused the rejection.
     * @param <E> The type of the exception.
     * @return A new promise that will be rejected with the same exception when the operation has completed.
     */
    <E extends Throwable> Promise<V> whenRejected(Class<E> exceptionClass, OnRejected<? super E> action);

    /**
     * Registers an operation that is to be performed when the promise is rejected using the specified
     * {@link java.util.concurrent.Executor} to execute the operation.
     * @param executor The executor that will be used to execute the operation.
     * @param action The operation that is performed when the promise is rejected accepting the exception that caused
     *               the rejection.
     * @return A new promise that will be rejected with the same exception when the operation has completed.
     */
    Promise<V> whenRejected(Executor executor, OnRejected<Throwable> action);

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class
     * using the specified {@link java.util.concurrent.Executor}.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param executor The executor that will be used to execute the operation.
     * @param action The operation that is performed when the promise is rejected with an exception that is of or
     *               subclasses the specified {@code exceptionClass}, accepting the exception that caused the rejection.
     * @param <E> The type of the exception.
     * @return A new promise that will be rejected with the same exception when the operation has completed.
     */
    <E extends Throwable> Promise<V> whenRejected(Class<E> exceptionClass, Executor executor, OnRejected<? super E> action);

    /**
     * Specifies a successful result to be used to fulfill the returned promise if the current promise is rejected.
     * @param result The value to use as a successful result.
     * @return A new promise that will be fulfilled with the {@code result} if the current promise is rejected.
     */
    Promise<V> handleWith(V result);

    /**
     * Specifies a successful result to be used to fulfill the returned promise if the current promise is rejected
     * with the specified exception class.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param result The value to use as a successful result.
     * @param <E> The type of the exception.
     * @return A new promise that will be fulfilled with the {@code result} if the current promise is rejected.
     */
    <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, V result);

    /**
     * Registers an operation that is to be performed when the promise is rejected that handles the rejection
     * exception and returns a successful result.
     * @param handler The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a successful result.
     * @return A new promise that will be fulfilled with the result of the operation if the current promise is
     *         rejected.
     */
    Promise<V> handleWith(OnRejectedHandler<Throwable, ? extends V> handler);

    /**
     * Registers an operation that is to be performed when the promise is rejected that handles the rejection
     * exception and returns a successful result using the specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param handler The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a successful result.
     * @return A new promise that will be fulfilled with the result of the operation if the current promise is
     *         rejected.
     */
    Promise<V> handleWith(Executor executor, OnRejectedHandler<Throwable, ? extends V> handler);

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class
     * that handles the rejection exception and returns a successful result.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param handler The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a successful result.
     * @param <E> The type of the exception.
     * @return A new promise that will be fulfilled with the result of the operation if the current promise is
     *         rejected.
     */
    <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends V> handler);

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class
     * that handles the rejection exception and returns a successful result using the specified
     * {@link java.util.concurrent.Executor}.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param executor The executor that will be used to execute the operation.
     * @param handler The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a successful result.
     * @param <E> The type of the exception.
     * @return A new promise that will be fulfilled with the result of the operation if the current promise is
     *         rejected.
     */
    <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, Executor executor, OnRejectedHandler<? super E, ? extends V> handler);

    /**
     * Registers an operation that is to be performed when the promise is rejected that handles the rejection
     * exception and returns a fallback operation.
     * @param fallback The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a fallback operation.
     * @return A new promise that will complete propagating the result of the fallback operation.
     */
    Promise<V> fallbackWith(OnRejectedHandler<Throwable, ? extends Future<V>> fallback);

    /**
     * Registers an operation that is to be performed when the promise is rejected that handles the rejection
     * exception and returns a fallback operation using the specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param fallback The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a fallback operation.
     * @return A new promise that will complete propagating the result of the fallback operation.
     */
    Promise<V> fallbackWith(Executor executor, OnRejectedHandler<Throwable, ? extends Future<V>> fallback);

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class
     * that handles the rejection exception and returns a successful result.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param fallback The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a fallback operation.
     * @param <E> The type of the exception.
     * @return A new promise that will complete propagating the result of the fallback operation.
     */
    <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends Future<V>> fallback);

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class
     * that handles the rejection exception and returns a successful result using the specified
     * {@link java.util.concurrent.Executor}.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param executor The executor that will be used to execute the operation.
     * @param fallback The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a fallback operation.
     * @param <E> The type of the exception.
     * @return A new promise that will complete propagating the result of the fallback operation.
     */
    <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, Executor executor, OnRejectedHandler<? super E, ? extends Future<V>> fallback);

    /**
     * Registers an operation that is to be performed when the promise is completed.
     * @param action The operation that is performed when the promise is completed.
     * @return A new promise that will be completed with the same result or exception as the current promise when
     * the callback operation has completed.
     */
    Promise<V> whenCompleted(OnCompleted<V> action);

    /**
     * Registers an operation that is to be performed when the promise is completed using the specified
     * {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param action The operation that is performed when the promise is completed.
     * @return A new promise that will be completed with the same result or exception as the current promise when
     * the callback operation has completed.
     */
    Promise<V> whenCompleted(Executor executor, OnCompleted<V> action);
}
