package org.jpromise;

public interface Cancellation {
    boolean isRequested();
    void throwIfRequested();
}
