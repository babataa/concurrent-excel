package com.babata.concurrent.excel.resolve.annotation;

import com.babata.concurrent.excel.resolve.CustomConvertor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * excel导出列名称、格式转换、排序、默认值
 * @author zqj
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {
    /**
     * 列名
     * @return
     */
    String name() default "";

    /**
     * 排序
     * @return
     */
    int index() default Integer.MAX_VALUE;

    /**
     * 默认值
     * @return
     */
    String defaultValue() default "";

    /**
     * 自定义转换器
     * @return
     */
    Class<? extends CustomConvertor> customConvertor() default CustomConvertor.class;

}
