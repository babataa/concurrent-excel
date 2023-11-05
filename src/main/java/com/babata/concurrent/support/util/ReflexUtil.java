package com.babata.concurrent.support.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author zqj
 */
public class ReflexUtil {

    public static Object parseObject(String value, Class<?> type) {
        if (String.class.isAssignableFrom(type)) {
            return value;
        } else if(StringUtils.isBlank(value)) {
            return null;
        } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
            return Integer.parseInt(value);
        } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
            return Long.parseLong(value);
        } else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
            return Double.parseDouble(value);
        } else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
            return Short.parseShort(value);
        } else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
            return Float.parseFloat(value);
        } else if (Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type)) {
            return Byte.parseByte(value);
        } else if (Byte[].class.isAssignableFrom(type) || byte[].class.isAssignableFrom(type)) {
            return value.getBytes();
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            return Boolean.parseBoolean(value);
        } else if (BigDecimal.class.isAssignableFrom(type)) {
            return new BigDecimal(value);
        } else if (BigInteger.class.isAssignableFrom(type)) {
            return new BigInteger(value);
        }
        return null;
    }
}
