package org.jpromise;

import org.jpromise.functions.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link org.jpromise.Promise} represents the asynchronous result of an operation.  Methods are provided to check if
 * the computation is complete, to register operations to be performed upon completion, to wait for its completion, and
 * to retrieve the result of the computation.  The registered operations return additional promise instances which
 * represent the completion of those operations, which can translate the results both synchronously and asynchronously.
 * The Cancellation is performed by the {@see #cancel()} method. Additional methods are provided to determine if the
 * task completed normally or was cancelled. Once a computation has completed, the computation cannot be cancelled.
 * @param <V> The result type of the promise.
 */
public abstract class Promise<V> implements Future<V> {
    /**
     * Gets the current state of the promise.
     * @see org.jpromise.PromiseState
     * @return The current state of the promise.
     */
    public abstract PromiseState state();

    /**
     * Gets whether the promise has completed.
     * @return {@code true} if the promise has completed; otherwise, {@code false}.
     */
    public boolean isDone() {
        return this.state() != PromiseState.PENDING;
    }

    /**
     * Gets whether the promise has resolved successfully.
     * @return {@code true} if the promise has resolved successfully; otherwise, {@code false}.
     */
    public boolean isResolved() {
        return this.state() == PromiseState.RESOLVED;
    }

    /**
     * Gets whether the promise has been rejected.
     * @return {@code true} if the promise has been rejected; otherwise, {@code false}.
     */
    public boolean isRejected() {
        return this.state() == PromiseState.REJECTED;
    }

    /**
     * Returns the result of the promise if it has resolved successfully or throws an exception if the promise
     * has been rejected.  If the promise is not yet completed immediately returns the {@code defaultValue}.
     * @param defaultValue The value to return if the promise is not completed.
     * @return The result of the promise if it has resolved successfully.
     * @throws ExecutionException The promise has rejected.
     * @throws CancellationException The promise has been cancelled.
     */
    public V getNow(V defaultValue) throws ExecutionException, CancellationException {
        if (!isDone()) {
            return defaultValue;
        }
        try {
            return this.get();
        }
        catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof CancellationException) {
                throw (CancellationException)cause;
            }
            throw exception;
        }
        catch (InterruptedException exception) {
            throw new ExecutionException(exception);
        }
    }

    /**
     * Cancels the promise after the specified timeout period.
     * @param mayInterruptIfRunning If the current promise is the result of a continuation operation specifies that
     *                              the thread on which the operation is executing can be interrupted.
     * @param timeout The maximum amount of time to wait before attempting to cancel the promise.
     * @param timeUnit The unit of time for the {@code timeout} argument.
     * @return A promise that will indicate if this promise was successfully cancelled.
     */
    public Promise<Boolean> cancelAfter(final boolean mayInterruptIfRunning, long timeout, TimeUnit timeUnit) {
        if (isDone()) {
            return Promise.resolved(false);
        }
        final Deferred<Boolean> deferred = Promise.defer();
        final Timer timer = new Timer(true);
        final AtomicBoolean flag = new AtomicBoolean();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (flag.compareAndSet(false, true)) {
                    deferred.resolve(Promise.this.cancel(mayInterruptIfRunning));
                }
            }
        }, timeUnit.toMillis(timeout));
        this.whenCompleted(new OnCompleted<V>() {
            @Override
            public void completed(Promise<V> promise, V result, Throwable exception) throws Throwable {
                if (flag.compareAndSet(false, true)) {
                    timer.cancel();
                    deferred.resolve(false);
                }
            }
        });
        return deferred.promise();
    }

    /**
     * Registers an operation that is to be performed when the promise is successfully resolved.
     * @param action The operation that is performed when the promise is successfully resolved accepting the result value.
     * @return A new promise that will be resolved with the same value when the operation has completed.
     */
    public Promise<V> then(OnResolved<? super V> action) {
        return this.then(PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    /**
     * Registers an operation that is to be performed when the promise is successfully resolved that transforms the
     * result synchronously into another value.
     * @param function The operation that is performed when the promise is successfully resolved accepting the result
     *                 value and transforming the result into a different value synchronously.
     * @param <V_APPLIED> The type of the transformed value.
     * @return A new promise that will be resolved when the operation has completed transforming the result.
     */
    public <V_APPLIED> Promise<V_APPLIED> thenApply(OnResolvedFunction<? super V, ? extends V_APPLIED> function) {
        return this.thenApply(PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, function);
    }

    /**
     * Registers an operation that is to be performed when the promise is successfully resolved that composed the result
     * into another value using another asynchronous operation.
     * @param function The operation that is performed when the promise is successfully resolved accepting the result
     *                 value and composing the result into a different value asynchronously.
     * @param <V_COMPOSED> The type of the composed value.
     * @return A new promise that will complete propagating the result of the composed operation.
     */
    public <V_COMPOSED> Promise<V_COMPOSED> thenCompose(OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function) {
        return this.thenCompose(PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, function);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected.
     * @param action The operation that is performed when the promise is rejected accepting the exception that caused
     *               the rejection.
     * @return A new promise that will be rejected with the same exception when the operation has completed.
     */
    public Promise<V> whenRejected(OnRejected<Throwable> action) {
        return this.whenRejected(Throwable.class, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param action The operation that is performed when the promise is rejected with an exception that is of or
     *               subclasses the specified {@code exceptionClass}, accepting the exception that caused the rejection.
     * @param <E> The type of the exception.
     * @return A new promise that will be rejected with the same exception when the operation has completed.
     */
    public <E extends Throwable> Promise<V> whenRejected(Class<E> exceptionClass, OnRejected<? super E> action) {
        return this.whenRejected(exceptionClass, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected using the specified
     * {@link java.util.concurrent.Executor} to execute the operation.
     * @param executor The executor that will be used to execute the operation.
     * @param action The operation that is performed when the promise is rejected accepting the exception that caused
     *               the rejection.
     * @return A new promise that will be rejected with the same exception when the operation has completed.
     */
    public Promise<V> whenRejected(Executor executor, OnRejected<Throwable> action) {
        return this.whenRejected(Throwable.class, executor, action);
    }

    /**
     * Specifies a successful result to be used to resolve the returned promise if the current promise is rejected.
     * @param result The value to use as a successful result.
     * @return A new promise that will be resolved with the {@code result} if the current promise is rejected.
     */
    public Promise<V> handleWith(V result) {
        return this.handleWith(Throwable.class, result);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected that handles the rejection
     * exception and returns a successful result.
     * @param handler The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a successful result.
     * @return A new promise that will be resolved with the result of the operation if the current promise is
     *         rejected.
     */
    public Promise<V> handleWith(OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, handler);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected that handles the rejection
     * exception and returns a successful result using the specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param handler The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a successful result.
     * @return A new promise that will be resolved with the result of the operation if the current promise is
     *         rejected.
     */
    public Promise<V> handleWith(Executor executor, OnRejectedHandler<Throwable, ? extends V> handler) {
        return this.handleWith(Throwable.class, executor, handler);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class
     * that handles the rejection exception and returns a successful result.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param handler The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a successful result.
     * @param <E> The type of the exception.
     * @return A new promise that will be resolved with the result of the operation if the current promise is
     *         rejected.
     */
    public <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends V> handler) {
        return this.handleWith(exceptionClass, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, handler);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected that handles the rejection
     * exception and returns a fallback operation.
     * @param fallback The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a fallback operation.
     * @return A new promise that will complete propagating the result of the fallback operation.
     */
    public Promise<V> fallbackWith(OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, fallback);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected that handles the rejection
     * exception and returns a fallback operation using the specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param fallback The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a fallback operation.
     * @return A new promise that will complete propagating the result of the fallback operation.
     */
    public Promise<V> fallbackWith(Executor executor, OnRejectedHandler<Throwable, ? extends Future<V>> fallback) {
        return this.fallbackWith(Throwable.class, executor, fallback);
    }

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class
     * that handles the rejection exception and returns a successful result.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param fallback The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a fallback operation.
     * @return A new promise that will complete propagating the result of the fallback operation.
     */
    public <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, OnRejectedHandler<? super E, ? extends Future<V>> fallback) {
        return this.fallbackWith(exceptionClass, PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, fallback);
    }

    /**
     * Registers an operation that is to be performed when the promise is completed.
     * @param action The operation that is performed when the promise is completed.
     * @return A new promise that will be completed with the same result or exception as the current promise when
     * the callback operation has completed.
     */
    public Promise<V> whenCompleted(OnCompleted<V> action) {
        return this.whenCompleted(PromiseExecutors.DEFAULT_CONTINUATION_EXECUTOR, action);
    }

    /**
     * Registers an operation that is to be performed when the promise is successfully resolved using the
     * specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param action The operation that is performed when the promise is successfully resolved accepting the result value.
     * @return A new promise that will be resolved with the same value when the operation has completed.
     */
    public abstract Promise<V> then(Executor executor, OnResolved<? super V> action);

    /**
     * Registers an operation that is to be performed when the promise is successfully resolved that transforms the
     * result synchronously into another value using the specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param function The operation that is performed when the promise is successfully resolved accepting the result
     *                 value and transforming the result into a different value synchronously.
     * @param <V_APPLIED> The type of the transformed value.
     * @return A new promise that will be resolved when the operation has completed transforming the result.
     */
    public abstract <V_APPLIED> Promise<V_APPLIED> thenApply(Executor executor, OnResolvedFunction<? super V, ? extends V_APPLIED> function);

    /**
     * Registers an operation that is to be performed when the promise is successfully resolved that composed the result
     * into another value using another asynchronous operation using the specified {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param function The operation that is performed when the promise is successfully resolved accepting the result
     *                 value and composing the result into a different value asynchronously.
     * @param <V_COMPOSED> The type of the composed value.
     * @return A new promise that will complete propagating the result of the composed operation.
     */
    public abstract <V_COMPOSED> Promise<V_COMPOSED> thenCompose(Executor executor, OnResolvedFunction<? super V, ? extends Future<V_COMPOSED>> function);

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
    public abstract <E extends Throwable> Promise<V> whenRejected(Class<E> exceptionClass, Executor executor, OnRejected<? super E> action);

    /**
     * Specifies a successful result to be used to resolve the returned promise if the current promise is rejected
     * with the specified exception class.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param result The value to use as a successful result.
     * @return A new promise that will be resolved with the {@code result} if the current promise is rejected.
     */
    public abstract <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, V result);

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
     * @return A new promise that will be resolved with the result of the operation if the current promise is
     *         rejected.
     */
    public abstract <E extends Throwable> Promise<V> handleWith(Class<E> exceptionClass, Executor executor, OnRejectedHandler<? super E, ? extends V> handler);

    /**
     * Registers an operation that is to be performed when the promise is rejected with the specified exception class
     * that handles the rejection exception and returns a successful result using the specified
     * {@link java.util.concurrent.Executor}.
     * @param exceptionClass The minimum class of which the rejected promise exception must be for the operation
     *                       to be performed.
     * @param executor The executor that will be used to execute the operation.
     * @param fallback The operation that is performed when the promise is rejected that handles the rejection
     *                exception and returns a fallback operation.
     * @return A new promise that will complete propagating the result of the fallback operation.
     */
    public abstract <E extends Throwable> Promise<V> fallbackWith(Class<E> exceptionClass, Executor executor, OnRejectedHandler<? super E, ? extends Future<V>> fallback);

    /**
     * Registers an operation that is to be performed when the promise is completed using the specified
     * {@link java.util.concurrent.Executor}.
     * @param executor The executor that will be used to execute the operation.
     * @param action The operation that is performed when the promise is completed.
     * @return A new promise that will be completed with the same result or exception as the current promise when
     * the callback operation has completed.
     */
    public abstract Promise<V> whenCompleted(Executor executor, OnCompleted<V> action);

    /**
     * Creates a new {@link org.jpromise.Deferred} operation that can be used to generate a {@link org.jpromise.Promise}
     * that is eventually completed.
     * @param <V> The type of the result of the promise.
     * @return The deferred operation.
     */
    public static <V> Deferred<V> defer() {
        return new DeferredPromise<V>();
    }

    /**
     * Creates a new {@link org.jpromise.Deferred} operation that can be used to generate a {@link org.jpromise.Promise}
     * that is eventually completed.  This method can be used to aid in generic type inference with the Java compiler.
     * @param resultClass The class representing the result type of the promise.
     * @param <V> The type of the result of the promise.
     * @return The deferred operation.
     */
    public static <V> Deferred<V> defer(Class<V> resultClass) {
        return new DeferredPromise<V>();
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that is already resolved.
     * @return A resolved promise.
     */
    public static Promise<Void> resolved() {
        return resolved(null);
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that is already resolved with the specified result.
     * @param result The result of the returned promise.
     * @param <V> The type of the result of the promise.
     * @return A resolved promise.
     */
    public static <V> Promise<V> resolved(V result) {
        Deferred<V> deferred = defer();
        deferred.resolve(result);
        return deferred.promise();
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that is already rejected with the specified exception.
     * @param exception The exception of the returned rejected promise.
     * @param <V> The type of the result of the promise.
     * @return A rejected promise.
     */
    public static <V> Promise<V> rejected(Throwable exception) {
        Deferred<V> deferred = defer();
        deferred.reject(exception);
        return deferred.promise();
    }

    /**
     * Returns a new {@link org.jpromise.Promise} that is already rejected with the specified exception.  This
     * method can be used to aid in generic type inference with the Java compiler.
     * @param resultClass The class of the result of the promise.
     * @param exception The exception of the returned rejected promise.
     * @param <V> The type of the result of the promise.
     * @return A rejected promise.
     */
    public static <V> Promise<V> rejected(Class<V> resultClass, Throwable exception) {
        return rejected(exception);
    }
}
