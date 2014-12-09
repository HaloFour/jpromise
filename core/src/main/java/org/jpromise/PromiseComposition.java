package org.jpromise;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public enum PromiseComposition implements PromiseCompositionListener {
    LISTENER {
        @Override
        public PromiseCallbackListener composingCallback(Promise<?> source, Promise<?> target) {
            return composite.composingCallback(source, target);
        }

        @Override
        public void exception(Throwable exception) {

        }
    };

    private static final Set<PromiseCompositionListener> listeners = new CopyOnWriteArraySet<>();
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
            List<PromiseCallbackListener> callbacks = new ArrayList<>(listeners.size());
            for (PromiseCompositionListener listener : listeners) {
                try {
                    PromiseCallbackListener callback = listener.composingCallback(source, target);
                    if (callback != null) {
                        callbacks.add(callback);
                    }
                }
                catch (Throwable exception) {
                    listener.exception(exception);
                }
            }
            return new ComposablePromiseCallbackListener(callbacks);
        }

        @Override
        public void exception(Throwable exception) { }
    }

    private static class ComposablePromiseCallbackListener implements PromiseCallbackListener {
        private final List<PromiseCallbackListener> callbacks;

        public ComposablePromiseCallbackListener(List<PromiseCallbackListener> callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public AutoCloseable invokingPromiseCallback(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
            List<AutoCloseable> closeables = new ArrayList<>(callbacks.size());
            for (PromiseCallbackListener callback : callbacks) {
                try {
                    AutoCloseable closeable = callback.invokingPromiseCallback(source, target, result, exception);
                    if (closeable != null) {
                        closeables.add(closeable);
                    }
                }
                catch (Throwable ignored) { }
            }
            return new CompositeAutoCloseable(closeables);
        }
    }

    private static class CompositeAutoCloseable implements AutoCloseable {
        private final List<AutoCloseable> closeables;

        public CompositeAutoCloseable(List<AutoCloseable> closeables) {
            this.closeables = closeables;
        }

        @Override
        public void close() throws Exception {
            for (AutoCloseable closeable : closeables) {
                try {
                    closeable.close();
                }
                catch (Throwable ignored) { }
            }
        }
    }
}
