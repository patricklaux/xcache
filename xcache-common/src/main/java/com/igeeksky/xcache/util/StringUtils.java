package com.igeeksky.xcache.util;

/**
 * 字符串工具类
 *
 * @author Patrick.Lau
 * @since 0.0.1
 */
public abstract class StringUtils {

    private StringUtils() {
    }

    /**
     * 如果字符串为空或者空白，返回true；否则返回false。
     */
    public static boolean isEmpty(final String str) {
        return (null == str || str.trim().isEmpty());
    }

    /**
     * 如果字符串不为空且非空白，返回true；否则返回false。
     */
    public static boolean isNotEmpty(final String str) {
        return (null != str && !(str.trim().isEmpty()));
    }


    public static String trim(String str) {
        if (null == str) {
            return null;
        }
        String temp = str.trim();
        if (temp.isEmpty()) {
            return null;
        }
        return temp;
    }


    public static String toUpperCase(String str) {
        String temp = trim(str);
        return (null != temp) ? temp.toUpperCase() : null;
    }


    public static String toLowerCase(String str) {
        String temp = trim(str);
        return (null != temp) ? temp.toLowerCase() : null;
    }


    public static String replaceFirstToLowerCase(String str) {
        char first = str.charAt(0);
        if (first >= 'A' && first <= 'Z') {
            char[] chars = str.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            return String.copyValueOf(chars);
        }
        return str;
    }


}
