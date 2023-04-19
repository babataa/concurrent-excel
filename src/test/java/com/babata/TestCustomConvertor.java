package com.babata;

import com.babata.concurrent.excel.resolve.CustomConvertor;

/**
 * @author zqj
 */
public class TestCustomConvertor implements CustomConvertor {
    @Override
    public String convert(Object o) {
        return "格式化后的：" + o.toString();
    }
}
