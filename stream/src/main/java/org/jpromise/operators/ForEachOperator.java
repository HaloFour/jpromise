package org.jpromise.operators;

import org.jpromise.functions.OnResolved;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class ForEachOperator<V> extends TerminalOperator<V, Void> {
    private final OnResolved<V> action;

    public ForEachOperator(OnResolved<V> action) {
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
        public void resolved(V result) throws Throwable {
            action.resolved(result);
        }

        @Override
        public Void completed() throws Throwable {
            return null;
        }
    }
}
