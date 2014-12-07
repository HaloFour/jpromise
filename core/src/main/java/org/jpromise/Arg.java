package org.jpromise;

import java.util.Arrays;

public class Arg {
    private static final String CLASS_NAME = Arg.class.getName();

    public static <V> V ensureNotNull(V arg, String name) {
        if (arg == null) {
            throw adjustStackTrace(new IllegalArgumentException(String.format("Argument \"%s\" must not be null.", name)));
        }
        return arg;
    }

    private static <E extends Throwable> E adjustStackTrace(E exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        int index = 0;
        for (int i = 0; i < stackTrace.length; i++) {
            String className = stackTrace[i].getClassName();
            if (className != CLASS_NAME) {
                stackTrace = Arrays.copyOfRange(stackTrace, i, stackTrace.length - i);
                exception.setStackTrace(stackTrace);
                break;
            }
        }
        return exception;
    }
}
