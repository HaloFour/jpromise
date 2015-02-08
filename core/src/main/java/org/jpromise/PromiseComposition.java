package org.jpromise;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class PromiseComposition {
    private PromiseComposition() {
        throw new IllegalStateException();
    }

    private static final Set<PromiseCompositionListener> listeners = new CopyOnWriteArraySet<PromiseCompositionListener>();
    private static final CompositePromiseCompositionListener composite = new CompositePromiseCompositionListener(listeners);

    public static boolean register(PromiseCompositionListener listener) {
        if (listener == null) throw new IllegalArgumentException(mustNotBeNull("listener"));
        return listeners.add(listener);
    }

    public static void clear() {
        listeners.clear();
    }

    static PromiseContinuationListener composingContinuation(Promise<?> source, Promise<?> target) {
        return composite.composingContinuation(source, target);
    }

    private static class CompositePromiseCompositionListener implements PromiseCompositionListener {
        private final Set<PromiseCompositionListener> listeners;

        public CompositePromiseCompositionListener(Set<PromiseCompositionListener> listeners) {
            this.listeners = listeners;
        }

        @Override
        public PromiseContinuationListener composingContinuation(Promise<?> source, Promise<?> target) {
            Map<PromiseCompositionListener, PromiseContinuationListener> callbacks = new HashMap<PromiseCompositionListener, PromiseContinuationListener>(listeners.size());
            for (PromiseCompositionListener listener : listeners) {
                try {
                    PromiseContinuationListener callback = listener.composingContinuation(source, target);
                    if (callback != null) {
                        callbacks.put(listener, callback);
                    }
                }
                catch (Throwable ignored) { }
            }
            return new CompositePromiseContinuationListener(callbacks);
        }
    }

    private static class CompositePromiseContinuationListener implements PromiseContinuationListener {
        private final Map<PromiseCompositionListener, PromiseContinuationListener> callbacks;

        public CompositePromiseContinuationListener(Map<PromiseCompositionListener, PromiseContinuationListener> callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public PromiseContinuationCompletion invokingContinuation(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
            Map<PromiseCompositionListener, PromiseContinuationCompletion> completions = new HashMap<PromiseCompositionListener, PromiseContinuationCompletion>(callbacks.size());
            for (Map.Entry<PromiseCompositionListener, PromiseContinuationListener> entry : callbacks.entrySet()) {
                PromiseCompositionListener listener = entry.getKey();
                PromiseContinuationListener callback = entry.getValue();
                try {
                    PromiseContinuationCompletion completion = callback.invokingContinuation(source, target, result, exception);
                    if (completion != null) {
                        completions.put(listener, completion);
                    }
                }
                catch (Throwable ignored) { }
            }
            return new CompositePromiseContinuationCompletion(completions);
        }
    }

    private static class CompositePromiseContinuationCompletion implements PromiseContinuationCompletion {
        private final Map<PromiseCompositionListener, PromiseContinuationCompletion> completions;

        public CompositePromiseContinuationCompletion(Map<PromiseCompositionListener, PromiseContinuationCompletion> completions) {
            this.completions = completions;
        }

        @Override
        public void completed(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
            for (Map.Entry<PromiseCompositionListener, PromiseContinuationCompletion> entry : completions.entrySet()) {
                PromiseContinuationCompletion completion = entry.getValue();
                try {
                    completion.completed(source, target, result, exception);
                }
                catch (Throwable ignore) { }
            }
        }
    }
}
