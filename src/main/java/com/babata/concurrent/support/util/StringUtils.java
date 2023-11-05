package com.babata.concurrent.support.util;

/**
 * @description:
 * @author: zqj
 */
public class StringUtils {

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否是数字
     */
    public static boolean isNumber(char c) {
        // ascii 编码
        return c >= 48 && c <= 57;
    }

}
