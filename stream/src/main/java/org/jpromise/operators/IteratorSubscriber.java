package org.jpromise.operators;

import org.jpromise.PromiseSubscriber;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

public class IteratorSubscriber<V> implements PromiseSubscriber<V>, Iterator<V> {
    private static abstract class Node<V> {
        public abstract V get();
    }

    private static class CompletedNode<V> extends Node<V> {
        @Override
        public V get() {
            return null;
        }
    }

    private final LinkedBlockingDeque<Node<V>> deque = new LinkedBlockingDeque<Node<V>>();
    private Node<V> current;

    public boolean hasNext() {
        current = null;
        try {
            Node<V> node = deque.take();
            if (node == null || node instanceof CompletedNode) {
                deque.add(new CompletedNode<V>());
                return false;
            }
            current = node;
            return true;
        }
        catch (InterruptedException ignored) { }
        return false;
    }

    @Override
    public V next() {
        Node<V> node = current;
        if (node == null) {
            throw new IllegalStateException();
        }
        return node.get();
    }

    @Override
    public void remove() { }

    @Override
    public void fulfilled(final V result) {
        Node<V> node = new Node<V>() {
            @Override
            public V get() {
                return result;
            }
        };
        deque.add(node);
    }

    @Override
    public void rejected(final Throwable exception) {
        Node<V> node = new Node<V>() {
            @Override
            public V get() {
                throw new RuntimeException(exception);
            }
        };
        deque.add(node);
    }

    @Override
    public void complete() {
        deque.add(new CompletedNode<V>());
    }
}
