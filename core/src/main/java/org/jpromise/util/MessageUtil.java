package org.jpromise.util;

import java.util.ResourceBundle;

public class MessageUtil {
    private static final String MUST_NOT_BE_NULL = "MUST_NOT_BE_NULL";
    private static final String NULL_OPERATION = "NULL_OPERATION";
    private static final String NULL_ACCUMULATOR = "NULL_ACCUMULATOR";
    private static final String DOES_NOT_SUPPORT_MULTIPLE_ACCUMULATORS = "DOES_NOT_SUPPORT_MULTIPLE_ACCUMULATORS";
    private static final ResourceBundle resources = ResourceBundle.getBundle("messages");

    private static String getMessage(String key) {
        return resources.getString(key);
    }

    public static String mustNotBeNull(String name) {
        return String.format(getMessage(MUST_NOT_BE_NULL), name);
    }

    public static String nullOperation() {
        return getMessage(NULL_OPERATION);
    }

    public static String nullAccumulator() {
        return getMessage(NULL_ACCUMULATOR);
    }

    public static String doesNotSupportMultipleAccumulators() {
        return getMessage(DOES_NOT_SUPPORT_MULTIPLE_ACCUMULATORS);
    }
}
