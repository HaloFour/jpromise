package org.jpromise;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public enum PromiseComposition implements PromiseCompositionListener {
    LISTENER {
        @Override
        public PromiseCallbackListener composingCallback(Promise<?> source, Promise<?> target) {
            return composite.composingCallback(source, target);
        }
    };

    private static final Set<PromiseCompositionListener> listeners = new CopyOnWriteArraySet<PromiseCompositionListener>();
    private static final CompositePromiseCompositionListener composite = new CompositePromiseCompositionListener(listeners);

    public static boolean register(PromiseCompositionListener listener) {
        if (listener == null) throw new IllegalArgumentException(mustNotBeNull("listener"));
        return listeners.add(listener);
    }

    public static void clear() {
        listeners.clear();
    }

    private static class CompositePromiseCompositionListener implements PromiseCompositionListener {
        private final Set<PromiseCompositionListener> listeners;

        public CompositePromiseCompositionListener(Set<PromiseCompositionListener> listeners) {
            this.listeners = listeners;
        }

        @Override
        public PromiseCallbackListener composingCallback(Promise<?> source, Promise<?> target) {
            Map<PromiseCompositionListener, PromiseCallbackListener> callbacks = new HashMap<PromiseCompositionListener, PromiseCallbackListener>(listeners.size());
            for (PromiseCompositionListener listener : listeners) {
                try {
                    PromiseCallbackListener callback = listener.composingCallback(source, target);
                    if (callback != null) {
                        callbacks.put(listener, callback);
                    }
                }
                catch (Throwable ignored) { }
            }
            return new ComposablePromiseCallbackListener(callbacks);
        }
    }

    private static class ComposablePromiseCallbackListener implements PromiseCallbackListener {
        private final Map<PromiseCompositionListener, PromiseCallbackListener> callbacks;

        public ComposablePromiseCallbackListener(Map<PromiseCompositionListener, PromiseCallbackListener> callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public PromiseCallbackCompletion invokingPromiseCallback(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
            Map<PromiseCompositionListener, PromiseCallbackCompletion> completions = new HashMap<PromiseCompositionListener, PromiseCallbackCompletion>(callbacks.size());
            for (Map.Entry<PromiseCompositionListener, PromiseCallbackListener> entry : callbacks.entrySet()) {
                PromiseCompositionListener listener = entry.getKey();
                PromiseCallbackListener callback = entry.getValue();
                try {
                    PromiseCallbackCompletion completion = callback.invokingPromiseCallback(source, target, result, exception);
                    if (completion != null) {
                        completions.put(listener, completion);
                    }
                }
                catch (Throwable ignored) { }
            }
            return new CompositePromiseCallbackCompletion(completions);
        }
    }

    private static class CompositePromiseCallbackCompletion implements PromiseCallbackCompletion {
        private final Map<PromiseCompositionListener, PromiseCallbackCompletion> completions;

        public CompositePromiseCallbackCompletion(Map<PromiseCompositionListener, PromiseCallbackCompletion> completions) {
            this.completions = completions;
        }

        @Override
        public void completed(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
            for (Map.Entry<PromiseCompositionListener, PromiseCallbackCompletion> entry : completions.entrySet()) {
                PromiseCallbackCompletion completion = entry.getValue();
                try {
                    completion.completed(source, target, result, exception);
                }
                catch (Throwable ignore) { }
            }
        }
    }
}
