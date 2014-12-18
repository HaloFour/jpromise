package org.jpromise;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class AsyncLocal<T> extends InheritableThreadLocal<T> {
    static {
        PromiseComposition.register(new AsyncLocalPromiseComposingListener());
    }

    static final LinkedBlockingDeque<WeakReference<AsyncLocal<?>>> list = new LinkedBlockingDeque<WeakReference<AsyncLocal<?>>>();

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
                state.put(reference, new WeakReference<Object>(value));
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
        public PromiseContinuationListener composingContinuation(Promise<?> source, Promise<?> target) {
            final AsyncLocalMap state = persistToMap();
            return new PersistedAsyncLocalState(state);
        }
    }

    private static class PersistedAsyncLocalState implements PromiseContinuationListener {
        private final AsyncLocalMap state;
        private final Thread thread;

        public PersistedAsyncLocalState(AsyncLocalMap state) {
            this.state = state;
            thread = Thread.currentThread();
        }

        @Override
        public PromiseContinuationCompletion invokingContinuation(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
            if (thread.equals(Thread.currentThread())) {
                return null;
            }
            AsyncLocalMap previous = persistToMap();
            restoreFromMap(state);
            return new RestoredAsyncLocalState(previous);
        }
    }

    private static class RestoredAsyncLocalState implements PromiseContinuationCompletion {
        private final AsyncLocalMap previous;

        public RestoredAsyncLocalState(AsyncLocalMap previous) {
            this.previous = previous;
        }

        @Override
        public void completed(Promise<?> source, Promise<?> target, Object result, Throwable exception) {
            restoreFromMap(previous);
        }
    }
}
