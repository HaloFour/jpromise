package org.jpromise;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AsyncLocal<T> extends InheritableThreadLocal<T> {
    static {
        PromiseComposition.register(new AsyncLocalPromiseComposingListener());
    }

    static final ConcurrentLinkedDeque<WeakReference<AsyncLocal<?>>> list = new ConcurrentLinkedDeque<>();

    public AsyncLocal() {
        list.add(new WeakReference<AsyncLocal<?>>(this));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Iterator<WeakReference<AsyncLocal<?>>> iterator = list.iterator();
        while (iterator.hasNext()) {
            WeakReference<AsyncLocal<?>> reference = iterator.next();
            AsyncLocal<?> local = reference.get();
            if (local == null || local == this) {
                iterator.remove();
            }
        }
    }

    private static AsyncLocalMap persistToMap() {
        AsyncLocalMap state = new AsyncLocalMap();
        Iterator<WeakReference<AsyncLocal<?>>> iterator = list.iterator();
        while (iterator.hasNext()) {
            WeakReference<AsyncLocal<?>> reference = iterator.next();
            AsyncLocal<?> local = reference.get();
            if (local == null) {
                iterator.remove();
                continue;
            }
            Object value = local.get();
            if (value != null) {
                state.put(reference, new WeakReference<>(value));
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private static void restoreFromMap(AsyncLocalMap state) {
        Iterator<WeakReference<AsyncLocal<?>>> iterator = list.iterator();
        while (iterator.hasNext()) {
            WeakReference<AsyncLocal<?>> reference = iterator.next();
            AsyncLocal local = reference.get();
            if (local == null) {
                iterator.remove();
                continue;
            }
            if (state != null && state.containsKey(reference)) {
                WeakReference<Object> valueReference = state.get(reference);
                Object value = valueReference.get();
                if (value != null) {
                    local.set(value);
                }
            }
        }
    }

    private static class AsyncLocalMap extends HashMap<WeakReference<AsyncLocal<?>>, WeakReference<Object>> { }

    private static class AsyncLocalPromiseComposingListener implements PromiseCompositionListener {
        @Override
        public PromiseCallbackListener composingCallback(Promise<?> source, Promise<?> target) {
            final AsyncLocalMap state = persistToMap();
            return new PersistedAsyncLocalState(state);
        }

        @Override
        public void exception(Throwable exception) {

        }
    }

    private static class PersistedAsyncLocalState implements PromiseCallbackListener {
        private final AsyncLocalMap state;
        private final Thread thread;

        public PersistedAsyncLocalState(AsyncLocalMap state) {
            this.state = state;
            thread = Thread.currentThread();
        }

        @Override
        public AutoCloseable invokingPromiseCallback(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
            if (thread.equals(Thread.currentThread())) {
                return null;
            }
            AsyncLocalMap previous = persistToMap();
            restoreFromMap(state);
            return new RestoredAsyncLocalState(previous);
        }
    }

    private static class RestoredAsyncLocalState implements AutoCloseable {
        private final AsyncLocalMap previous;

        public RestoredAsyncLocalState(AsyncLocalMap previous) {
            this.previous = previous;
        }

        @Override
        public void close() throws Exception {
            restoreFromMap(previous);
        }
    }
}
