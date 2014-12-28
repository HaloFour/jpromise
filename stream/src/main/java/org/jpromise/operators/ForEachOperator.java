package org.jpromise.operators;

import org.jpromise.functions.OnFulfilled;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class ForEachOperator<V> extends TerminalOperator<V, Void> {
    private final OnFulfilled<V> action;

    public ForEachOperator(OnFulfilled<V> action) {
        if (action == null) throw new IllegalArgumentException(mustNotBeNull("action"));
        this.action = action;
    }

    @Override
    protected TerminalOperation<V, Void> operation() {
        return new ForEachOperation();
    }

    private class ForEachOperation implements TerminalOperation<V, Void> {
        @Override
        public void start() throws Throwable { }

        @Override
        public void fulfilled(V result) throws Throwable {
            action.fulfilled(result);
        }

        @Override
        public Void completed() throws Throwable {
            return null;
        }
    }
}
