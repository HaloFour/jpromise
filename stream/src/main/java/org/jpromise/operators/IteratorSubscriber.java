package org.jpromise.operators;

import org.jpromise.PromiseSubscriber;

import java.util.Iterator;
import java.util.NoSuchElementException;
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
    private Node<V> next;

    public boolean hasNext() {
        if (next == null) {
            next = moveNext();
        }
        return !(next instanceof CompletedNode);
    }

    @Override
    public V next() {
        if (next == null) {
            next = moveNext();
        }
        if (next instanceof CompletedNode) {
            throw new NoSuchElementException();
        }
        Node<V> node = next;
        next = null;
        return node.get();
    }

    private Node<V> moveNext() {
        try {
            return deque.take();
        }
        catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

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
        Node<V> completed = new CompletedNode<V>();
        completed.get();
        deque.add(completed);
    }
}
