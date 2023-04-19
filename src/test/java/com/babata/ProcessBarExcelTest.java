package com.babata;

import com.babata.concurrent.excel.ExcelUtil;
import com.babata.concurrent.excel.context.ProgressbarContext;

import java.util.ArrayList;

/**
 * @author zqj
 */
public class ProcessBarExcelTest {

    public static void main(String[] args) {

    }

    /**
     * 带进度查询的http直接导出
     */
    private static void export1() {
        ProgressbarContext context = new ProgressbarContext();
        ExcelUtil.buildBarHttpDownLoadHandler(() -> {
                    //查库，分页插件只对方法的第一条sql生效
                    return null;
                }, 2000, 50000, context)
                .execute(null, ThreadPool.pool);
        System.out.println("当前进度" + context.getProgress());
    }

    /**
     * 自定义分页带进度条的导出
     * @throws InterruptedException
     */
    private static void export2() throws InterruptedException {
        ProgressbarContext context = new ProgressbarContext();
        ExcelUtil.buildSimpleBarHttpDownLoadHandler(() -> {
                    //查总数
                    return 0;
                }, 2000, 50000, context)
                .customSelect(batchParam -> {
            //分页查询（batchIndex：页数，batchSize：每页数量，batchStart：当前页开始的条数）
                    return new ArrayList<>();
                })
                .build()
                .execute(null, ThreadPool.pool);
        System.out.println("当前进度" + context.getProgress());
    }
}
