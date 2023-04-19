/**
 * Aliyun.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.babata.concurrent.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 数字类型工具类
 * @author zqj
 */
public class NumberUtil {
    public static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final Map<String, DecimalFormat> decimalFormatMap = new HashMap<>();
    private static Unsafe UNSAFE;
    //线程数组元素偏移量
    private static final int REF_ELEMENT_SHIFT;
    //线程数组第一个元素起始偏移量
    private static final long REF_ARRAY_BASE;

    static {
        //由于volatile无法保证数组元素的可见性，这里使用Unsafe工具类
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE =  (Unsafe) theUnsafe.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("unsafe init error");
        }
        //线程数组每个元素大小
        final int scale = UNSAFE.arrayIndexScale(int[].class);
        if (4 == scale) {
            //32位占4个字节
            REF_ELEMENT_SHIFT = 2;
        } else if (8 == scale) {
            //64位占8个字节
            REF_ELEMENT_SHIFT = 3;
        } else {
            throw new IllegalStateException("Unknown pointer size");
        }
        //数组对象的起始偏移量
        REF_ARRAY_BASE = UNSAFE.arrayBaseOffset(int[].class);
    }

    private NumberUtil(){}

    /**
     * 将c扩大到离它最近的2的倍数
     * @param c
     * @return
     */
    public static int sizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    public static String formatNumber(Number number, String pattern) {
        DecimalFormat decimalFormat = decimalFormatMap.get(pattern);
        if(decimalFormat == null) {
            synchronized (decimalFormatMap) {
                decimalFormat = decimalFormatMap.computeIfAbsent(pattern, pattern0 -> new DecimalFormat(pattern0));
            }
        }
        return decimalFormat.format(number);
    }

    /**
     * 对数组元素的可见性set
     * @param index
     * @param value
     */
    public static void setAt(int[] stack, int index, int value) {
        UNSAFE.putIntVolatile(stack, REF_ARRAY_BASE + (index << REF_ELEMENT_SHIFT), value);
    }

    /**
     * 对数组元素的可见性get
     * @param index
     * @return
     */
    public static int getAt(int[] stack, int index) {
        return UNSAFE.getIntVolatile(stack, REF_ARRAY_BASE + (index << REF_ELEMENT_SHIFT));
    }

    /**
     * CAS set
     * @param index
     * @param c
     * @param v
     * @return
     */
    public static boolean casAt(int[] stack, int index, int c, int v) {
        return UNSAFE.compareAndSwapInt(stack, REF_ARRAY_BASE + (index << REF_ELEMENT_SHIFT), c, v);
    }

    /**
     * 计算二进制中1的个数
     * @param n
     * @return
     */
    public static int hammingWeight(int n) {
        int ans = 0;
        while (n != 0) {
            ans += (n & 1);
            n >>>= 1;
        }
        return ans;
    }
}
