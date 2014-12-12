package org.jpromise.operators;

import org.jpromise.OnSubscribe;
import org.jpromise.PromiseCollector;

import static org.jpromise.util.MessageUtil.*;

public class CollectOperator<V, A, R> extends TerminalOperator<V, R> {
    private final PromiseCollector<V, A, R> collector;

    public CollectOperator(OnSubscribe<V> subscribe, PromiseCollector<V, A, R> collector) {
        super(subscribe);
        this.collector = collector;
    }

    @Override
    protected TerminalOperation<V, R> operation() {
        return new CollectorOperation<>(collector);
    }

    private static class CollectorOperation<V, A, R> implements TerminalOperation<V, R> {
        private final PromiseCollector<V, A, R> collector;
        private A accumulator;

        public CollectorOperation(PromiseCollector<V, A, R> collector) {
            this.collector = collector;
        }

        @Override
        public void start() throws Throwable {
            accumulator = collector.getAccumulator();
            if (accumulator == null) {
                throw new IllegalStateException(nullAccumulator());
            }
        }

        @Override
        public void resolved(V result) throws Throwable {
            collector.accumulate(accumulator, result);
        }

        @Override
        public R completed() throws Throwable {
            return collector.finish(accumulator);
        }
    }
}
