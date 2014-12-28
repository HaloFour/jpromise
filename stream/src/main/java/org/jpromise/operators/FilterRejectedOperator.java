package org.jpromise.operators;

import org.jpromise.functions.OnRejectedHandler;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class FilterRejectedOperator<V, E extends Throwable> extends BoundedStreamOperator<V, V> {
    private final Class<E> exceptionClass;
    private final OnRejectedHandler<? super E, Boolean> predicate;

    public FilterRejectedOperator(Class<E> exceptionClass) {
        this(exceptionClass, new OnRejectedHandler<E, Boolean>() {
            @Override
            public Boolean handle(E exception) throws Throwable {
                return true;
            }
        });
    }

    public FilterRejectedOperator(Class<E> exceptionClass, OnRejectedHandler<? super E, Boolean> predicate) {
        if (exceptionClass == null) throw new IllegalArgumentException(mustNotBeNull("exceptionClass"));
        if (predicate == null) throw new IllegalArgumentException(mustNotBeNull("predicate"));
        this.exceptionClass = exceptionClass;
        this.predicate = predicate;
    }

    @Override
    protected void fulfilled(BoundedPromiseSubscriber<V> subscriber, V result) throws Throwable {
        subscriber.fulfilled(result);
    }

    @Override
    protected void rejected(BoundedPromiseSubscriber<V> subscriber, Throwable exception) throws Throwable {
        if (exceptionClass.isInstance(exception)) {
            E typed = exceptionClass.cast(exception);
            Boolean filter = predicate.handle(typed);
            if (filter == null || !filter) {
                subscriber.rejected(exception);
            }
            else {
                subscriber.omit();
            }
        }
        else {
            subscriber.rejected(exception);
        }
    }
}
