package org.jpromise;

import org.jpromise.functions.FutureGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jpromise.util.MessageUtil.mustNotBeNull;

public class PromiseStreams {
    private PromiseStreams() {
        throw new IllegalStateException();
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(1);
        list.add(promise1);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1, Promise<V> promise2) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(2);
        list.add(promise1);
        list.add(promise2);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(3);
        list.add(promise1);
        list.add(promise2);
        list.add(promise3);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(4);
        list.add(promise1);
        list.add(promise2);
        list.add(promise3);
        list.add(promise4);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V> promise1, Promise<V> promise2, Promise<V> promise3, Promise<V> promise4, Promise<V> promise5) {
        List<Promise<V>> list = new ArrayList<Promise<V>>(5);
        list.add(promise1);
        list.add(promise2);
        list.add(promise3);
        list.add(promise4);
        list.add(promise5);
        return new PromiseSource<V>(list);
    }

    public static <V> PromiseStream<V> from(Promise<V>[] promises) {
        return new PromiseSource<V>(promises);
    }

    public static <V> PromiseStream<V> from(Iterable<Promise<V>> promises) {
        return new PromiseSource<V>(promises);
    }

    public static <V> PromiseStream<V> generate(FutureGenerator<V> generator) {
        if (generator == null) throw new IllegalArgumentException(mustNotBeNull("generator"));
        return new GeneratorSource<V>(generator);
    }

    public static <V> PromiseStream<V> empty() {
        return from(Collections.<Promise<V>>emptyList());
    }

    public static <V> PromiseStream<V> empty(Class<V> elementClass) {
        return empty();
    }

    public static <V> PromiseStream<V> never() {
        return new AbstractPromiseStream<V>() {
            @Override
            public Promise<Void> subscribe(PromiseSubscriber<? super V> subscriber) {
                if (subscriber == null) throw new IllegalArgumentException(mustNotBeNull("subscriber"));
                return Promises.defer(Void.class).promise();
            }
        };
    }

    public static <V> PromiseStream<V> never(Class<V> elementClass) {
        return never();
    }

    public static <V> PromiseStream<V> single(final V result) {
        return new AbstractPromiseStream<V>() {
            @Override
            public Promise<Void> subscribe(PromiseSubscriber<? super V> subscriber) {
                if (subscriber == null) throw new IllegalArgumentException(mustNotBeNull("subscriber"));
                subscriber.fulfilled(result);
                subscriber.complete();
                return Promises.fulfilled();
            }
        };
    }

    public static <V> PromiseStream<V> rejected(final Throwable exception) {
        return new AbstractPromiseStream<V>() {
            @Override
            public Promise<Void> subscribe(PromiseSubscriber<? super V> subscriber) {
                if (subscriber == null) throw new IllegalArgumentException(mustNotBeNull("subscriber"));
                subscriber.rejected(exception);
                subscriber.complete();
                return Promises.fulfilled();
            }
        };
    }

    public static <V> PromiseStream<V> rejected(Class<V> elementClass, Throwable exception) {
        return rejected(exception);
    }
}
