package org.jpromise.util;

import java.util.ResourceBundle;

public class MessageUtil {
    private static final ResourceBundle resources = ResourceBundle.getBundle("messages");

    private static String getMessage(String key) {
        return resources.getString(key);
    }

    public static String mustNotBeNull(String name) {
        return String.format(getMessage("MUST_NOT_BE_NULL"), name);
    }
}
