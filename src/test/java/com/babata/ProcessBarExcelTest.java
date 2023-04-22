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
    private static void export1() throws InterruptedException {
        ProgressbarContext context = new ProgressbarContext();
        ExcelUtil.buildBarHttpDownLoadHandler(() -> {
                    //查库，分页插件只对方法的第一条sql生效
                    return null;
                }, 2000, 50000, context)
                //开启分片下载，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
                .partition(true)
                //并发分片的限制，即同时写的文件数量限制在设定的值，开启分片后默认20
                .partitionLimit(10)
                .execute(ThreadPool.pool);
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
                //开启分片下载，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
                .partition(true)
                //并发分片的限制，即同时写的文件数量限制在设定的值，开启分片后默认20
                .partitionLimit(10)
                .build()
                .execute(ThreadPool.pool);
        System.out.println("当前进度" + context.getProgress());
    }
}
