package org.jpromise.util;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ResourceBundle;

public class MessageUtil {
    private MessageUtil() {
        throw new IllegalStateException();
    }

    private static final String MUST_NOT_BE_NULL = "MUST_NOT_BE_NULL";
    private static final String DOES_NOT_IMPLEMENT_EXECUTOR = "DOES_NOT_IMPLEMENT_EXECUTOR";
    private static final String METHOD_NOT_STATIC = "METHOD_NOT_STATIC";
    private static final String METHOD_DOES_NOT_RETURN_EXECUTOR = "METHOD_DOES_NOT_RETURN_EXECUTOR";
    private static final String FIELD_NOT_STATIC = "FIELD_NOT_STATIC";
    private static final String FIELD_IS_NOT_EXECUTOR = "FIELD_IS_NOT_EXECUTOR";
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

    public static String doesNotImplementExecutor(Class<?> cls) {
        return String.format(getMessage(DOES_NOT_IMPLEMENT_EXECUTOR), cls.getName());
    }

    public static String methodNotStatic(Method method) {
        return String.format(getMessage(METHOD_NOT_STATIC), method.getName(), method.getDeclaringClass().getName());
    }

    public static String methodDoesNotReturnExecutor(Method method) {
        return String.format(getMessage(METHOD_DOES_NOT_RETURN_EXECUTOR), method.getName(), method.getDeclaringClass().getName());
    }

    public static String fieldNotStatic(Field field) {
        return String.format(getMessage(FIELD_NOT_STATIC), field.getName(), field.getDeclaringClass().getName());
    }

    public static String fieldIsNotExecutor(Field field) {
        return String.format(getMessage(FIELD_IS_NOT_EXECUTOR), field.getName(), field.getDeclaringClass().getName());
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
