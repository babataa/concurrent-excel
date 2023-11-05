package com.babata.concurrent.excel.resolve;

import com.babata.concurrent.excel.model.ExcelExportAble;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * excel列上下文
 * @author: zqj
 */
public class ColumnContext {
    /**
     * 列名
     */
    String name;

    /**
     * 列格式转换
     */
    Function<ExcelExportAble, String> fun;

    BiConsumer<Object, String> parseObjectFun;

    public ColumnContext() {
    }

    public ColumnContext(String name, Function<ExcelExportAble, String> fun) {
        this.name = name;
        this.fun = fun;
    }

    public ColumnContext(String name, BiConsumer<Object, String> parseObjectFun) {
        this.name = name;
        this.parseObjectFun = parseObjectFun;
    }

    public String getName() {
        return name;
    }

    public Function<ExcelExportAble, String> getFun() {
        return fun;
    }

    public BiConsumer<Object, String> getParseObjectFun() {
        return parseObjectFun;
    }
}
