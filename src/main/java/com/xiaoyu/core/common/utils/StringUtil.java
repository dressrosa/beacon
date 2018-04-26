package com.xiaoyu.core.common.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

public class StringUtil extends StringUtils {

    private static final char SEPARATOR = '_';
    private static final String CHARSET_NAME = "UTF-8";

    /**
     * 转换为字节数组
     * 
     * @param str
     * @return
     */
    public static byte[] getBytes(String str) {
        if (str != null) {
            try {
                return str.getBytes(StringUtil.CHARSET_NAME);
            } catch (final UnsupportedEncodingException e) {
                return new byte[0];
            }
        } else {
            return new byte[0];
        }
    }

    /**
     * 转换为字节数组
     * 
     * @param str
     * @return
     */
    public static String toString(byte[] bytes) {
        try {
            return new String(bytes, StringUtil.CHARSET_NAME);
        } catch (final UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 驼峰命名法工具
     * 
     * @return toCamelCase("hello_world") == "helloWorld"
     *         toCapitalizeCamelCase("hello_world") == "HelloWorld"
     *         toUnderScoreCase("helloWorld") = "hello_world"
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }

        s = s.toLowerCase();

        final StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);

            if (c == StringUtil.SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String lowerFirstChar(String s) {
        if (s == null) {
            return null;
        }
        char first = s.charAt(0);
        if (Character.isLowerCase(first)) {
            return s;
        }
        first = Character.toLowerCase(first);
        final StringBuilder sb = new StringBuilder(s.length());
        sb.append(first);
        sb.append(s.substring(1));
        return sb.toString();
    }

    /**
     * 驼峰命名法工具
     * 
     * @return toCamelCase("hello_world") == "helloWorld"
     *         toCapitalizeCamelCase("hello_world") == "HelloWorld"
     *         toUnderScoreCase("helloWorld") = "hello_world"
     */
    public static String toCapitalizeCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = StringUtil.toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * 驼峰命名法工具
     * 
     * @return toCamelCase("hello_world") == "helloWorld"
     *         toCapitalizeCamelCase("hello_world") == "HelloWorld"
     *         toUnderScoreCase("helloWorld") = "hello_world"
     */
    public static String toUnderScoreCase(String s) {
        if (s == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);

            boolean nextUpperCase = true;

            if (i < (s.length() - 1)) {
                nextUpperCase = Character.isUpperCase(s.charAt(i + 1));
            }

            if ((i > 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    sb.append(StringUtil.SEPARATOR);
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    public static boolean isIP(String ip) {
        if ("localhost".equals(ip)) {
            return true;
        }
        // 定义正则表达式
        String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        if (ip.matches(regex)) {
            return true;
        } else {
            return false;
        }
    }

}
