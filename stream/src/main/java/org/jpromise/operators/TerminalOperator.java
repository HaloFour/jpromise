package org.jpromise.operators;

import org.jpromise.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.jpromise.util.MessageUtil.nullOperation;

public abstract class TerminalOperator<V, R> {

    protected abstract TerminalOperation<V, R> operation();

    public final Promise<R> subscribe(PromiseStream<V> stream) {
        TerminalOperation<V, R> operation = operation();
        if (operation == null) {
            throw new IllegalStateException(nullOperation());
        }
        TerminalPromiseSubscriber<V, R> subscriber = new TerminalPromiseSubscriber<V, R>(operation);
        stream.subscribe(subscriber);
        return subscriber.promise();
    }

    private static class TerminalPromiseSubscriber<V, R> implements PromiseSubscriber<V> {
        private final Deferred<R> deferred = Promises.defer();
        private final Promise<R> promise = deferred.promise();
        private final TerminalOperation<V, R> operation;
        private final AtomicBoolean first = new AtomicBoolean(true);

        public TerminalPromiseSubscriber(TerminalOperation<V, R> operation) {
            this.operation = operation;
        }

        @Override
        public synchronized void fulfilled(V result) {
            try {
                if (first.compareAndSet(true, false)) {
                    operation.start();
                }
                if (!promise.isDone()) {
                    operation.fulfilled(result);
                }
            }
            catch (Throwable exception) {
                rejected(exception);
            }
        }

        @Override
        public void rejected(Throwable exception) {
            if (!promise.isDone()) {
                deferred.reject(exception);
            }
        }

        @Override
        public void complete() {
            try {
                if (first.compareAndSet(true, false)) {
                    operation.start();
                }
                if (!promise.isDone()) {
                    R result = operation.completed();
                    deferred.fulfill(result);
                }
            }
            catch (Throwable exception) {
                deferred.reject(exception);
            }
        }

        public Promise<R> promise() {
            return promise;
        }
    }
}
