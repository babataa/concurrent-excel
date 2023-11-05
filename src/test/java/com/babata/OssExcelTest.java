package com.babata;

import com.babata.concurrent.excel.ExcelUtil;
import com.babata.concurrent.excel.context.ProgressbarContext;
import com.babata.concurrent.support.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;

public class OssExcelTest {

    public static void main(String[] args) throws InterruptedException {
        //自定义分页OSS导出
        customPageExport();
        //自动分页OSS导出
        //autoPageOssExport();
    }

    /**
     * 自动分页OSS上传
     */
    private static void autoPageOssExport() {
        ProgressbarContext context = new ProgressbarContext();
        ExcelUtil.buildOssDownLoadHandler(() -> new ArrayList<>(), 2000, 50000, ThreadPool.pool, context).setOssAction(inputStream -> {});
    }

    /**
     * 自定义分页OSS上传
     * @throws InterruptedException
     */
    private static void customPageExport() throws InterruptedException {
        ProgressbarContext context = new ProgressbarContext();
        int count = 1000001;
        long start = System.currentTimeMillis();
        new Thread(() -> {
            try {
                ExcelUtil.buildSimpleOssDownLoadProcessorBuilder(() -> count
                                , 2000, 50000, null, context)
                        .ossAction(inputStream -> {
                            //通过inputStream上传OSS操作
                            System.out.println("开始上传OSS：inputStream" + inputStream);
                            try {
                                //模拟OSS上传
                                FileUtils.uploadByPipedStream(inputStream, context.getTableName() + ".zip");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("导出完成");
                        })
                        //进度条粒度，50代表每完成2%刷新一次进度，默认20，即每完成5%刷新一次进度
                        .processPart(50)
                        //自定义分页获取数据的方法
                        .customSelect(batchParam -> DataFactory.get(batchParam, count))
                        //分片并发任务方式，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
                        .partition(true)
                        //并发分片的限制，即同时写的文件数量限制在设定的值，开启分片后默认20
                        .partitionLimit(20)
                        .build()
                        .addExceptionHandler(exception -> {
                            context.setProgress(-1);
                        })
                        .execute(ThreadPool.pool);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();;
        while(context.getProgress() != 100 && context.getProgress() != -1) {
            System.out.println("当前进度：" + context.getProgress());
            Thread.sleep(1000L);
        }
        System.out.println(context.getProgress());
        System.out.println(System.currentTimeMillis() - start);
    }

}
