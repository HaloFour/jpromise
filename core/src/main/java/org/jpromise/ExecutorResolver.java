package org.jpromise;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Executor;

import static java.util.Locale.ENGLISH;
import static org.jpromise.util.MessageUtil.*;

public class ExecutorResolver {
    private ExecutorResolver() {
        throw new IllegalStateException();
    }

    public static Executor resolveBySetting(String settingName, Executor defaultExecutor) {
        String settingValue = System.getProperty(settingName);
        if (settingValue == null || settingValue.length() == 0) {
            return defaultExecutor;
        }
        String lowerValue = settingValue.toLowerCase(ENGLISH);
        if (lowerValue.equals(PromiseExecutors.COMMON_POOL_KEY)) {
            return PromiseExecutors.COMMON_POOL;
        }
        else if (lowerValue.equals(PromiseExecutors.CURRENT_THREAD_KEY)) {
            return PromiseExecutors.CURRENT_THREAD;
        }
        else if (lowerValue.equals(PromiseExecutors.NEW_THREAD_KEY)) {
            return PromiseExecutors.NEW_THREAD;
        }
        Executor executor = null;
        try {
            executor = resolveByName(settingValue);
        }
        catch (Throwable exception) {
            return defaultExecutor;
        }
        return executor;
    }

    public static Executor resolveByName(String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
        if (name == null) throw new IllegalArgumentException(mustNotBeNull("name"));
        int memberSeparator = name.indexOf('#');
        if (memberSeparator == -1) {
            return resolveByClassName(name);
        }
        else {
            String className = name.substring(0, memberSeparator);
            String memberName = name.substring(memberSeparator + 1);
            return resolveByClassNameAndMemberName(className, memberName);
        }
    }

    static Executor resolveByClassName(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<Executor> executorClass = Executor.class;
        Class<?> cls = null;
        cls = Class.forName(className);
        if (executorClass.isAssignableFrom(cls)) {
            return (Executor) cls.newInstance();
        }
        throw new IllegalArgumentException(doesNotImplementExecutor(cls));
    }

    static Executor resolveByClassNameAndMemberName(String className, String memberName) throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> cls = Class.forName(className);
        if (memberName.endsWith("()")) {
            int length = memberName.length();
            String methodName = memberName.substring(0, length - 2);
            return resolveByClassAndMethodName(cls, methodName);
        }
        return resolveByClassAndFieldName(cls, memberName);
    }

    static Executor resolveByClassAndMethodName(Class<?> cls, String methodName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = cls.getMethod(methodName);
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        int modifiers = method.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException(methodNotStatic(method));
        }
        Class<?> returnType = method.getReturnType();
        if (Executor.class.isAssignableFrom(returnType)) {
            return (Executor)method.invoke(null);
        }
        throw new IllegalArgumentException(methodDoesNotReturnExecutor(method));
    }

    static Executor resolveByClassAndFieldName(Class<?> cls, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = cls.getField(fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        int modifiers = field.getModifiers();
        if (!Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException(fieldNotStatic(field));
        }
        Class<?> fieldType = field.getType();
        if (!Executor.class.isAssignableFrom(fieldType)) {
            throw new IllegalArgumentException(fieldIsNotExecutor(field));
        }
        return (Executor)field.get(null);
    }
}