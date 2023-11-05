package com.babata.concurrent.excel.resolve;

/**
 * 自定义列格式转换
 * @author: zqj
 */
public interface CustomConvertor<T> {
    default String convert(T t) {
        return null;
    }

    default T parse(String value) {
        return null;
    }
}
