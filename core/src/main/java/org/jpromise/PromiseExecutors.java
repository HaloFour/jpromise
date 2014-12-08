package org.jpromise;

import java.util.concurrent.Executor;

public enum PromiseExecutors implements Executor {
    CURRENT {
        @Override
        public void execute(Runnable command) {
            if (command != null) {
                command.run();
            }
        }
    },
    NEW {
        @Override
        public void execute(Runnable command) {
            if (command != null) {
                Thread thread = new Thread(command);
                thread.setDaemon(true);
                thread.start();
            }
        }
    }
}

