package org.jpromise.operators;

public interface OutstandingOperation extends AutoCloseable {
    void close();
}
