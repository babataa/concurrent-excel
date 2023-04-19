package com.babata.concurrent.excel.resolve;

/**
 * 自定义列格式转换
 * @author: zqj
 */
public interface CustomConvertor<T> {
    String convert(T t);
}
