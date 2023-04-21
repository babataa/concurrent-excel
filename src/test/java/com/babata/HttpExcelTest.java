package com.babata;

import com.babata.concurrent.excel.ExcelUtil;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zqj
 */
public class HttpExcelTest {

    public static void main(String[] args) throws InterruptedException {

    }

    /**
     * http直接导出
     * @throws InterruptedException
     */
    private static void export1() throws InterruptedException {
        ExcelUtil.buildHttpDownLoadHandler(() -> {
            //查库，分页插件只对方法的第一条sql生效
            return null;
        }, 2000, 50000)
                //开启分片下载，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
                .partition(true)
                //并发分片的限制，即同时写的文件数量限制在设定的值，开启分片后默认20
                .partitionLimit(10)
                .addExceptionHandler(exception -> {
                    //异步异常处理，并发任务只会进入一次异常处理
                })
                .execute(null, ThreadPool.pool);
    }

    /**
     * http自定义分页导出
     * @throws InterruptedException
     */
    private static void export3() throws InterruptedException {
        int count = 0;
        ExcelUtil.buildSimpleHttpDownLoadBuilder(() -> {
            //查总数方法
            return count;
        }, 2000, 50000)
                .customSelect(batchParam -> {
                    //分页查询（batchIndex：页数，batchSize：每页数量，batchStart：当前页开始的条数）
                    return new ArrayList<>();
                })
                //开启分片下载，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
                .partition(true)
                //并发分片的限制，即同时写的文件数量限制在设定的值，开启分片后默认20
                .partitionLimit(10)
                .build()
                .execute(null, ThreadPool.pool);
    }
}
