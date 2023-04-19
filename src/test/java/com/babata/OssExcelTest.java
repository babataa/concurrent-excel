package com.babata;

import com.babata.concurrent.excel.ExcelUtil;
import com.babata.concurrent.excel.context.ProgressbarContext;
import com.babata.concurrent.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OssExcelTest {

    public static void main(String[] args) throws InterruptedException {
        //自定义分页OSS导出
        customPageExport();
        //自动分页OSS导出
        //autoPageOssExport();
    }

    /**
     * 自定义分页OSS上传
     * @throws InterruptedException
     */
    private static void customPageExport() throws InterruptedException {
        ProgressbarContext context = new ProgressbarContext();
        int count = 10000000;
        new Thread(() -> {
            try {
                ExcelUtil.buildSimpleOssDownLoadProcessorBuilder(() -> count
                                , 2000, 500000, null, context)
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
                        .customSelect(batchParam -> DataFactory.get(batchParam, count))
                        //分片方式，显示的进度可能会有一定延迟，因为并发下载excel是以倒数第二个文件进度为准，有可能第二个文件先完成
                        .partition(true)
                        .build()
                        .addExceptionHandler(exception -> {
                            //异步异常处理
                        })
                        .execute(null, ThreadPool.pool);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();;
        while(context.getProgress() != 100) {
            System.out.println("当前进度：" + context.getProgress());
            Thread.sleep(1000L);
        }
        System.out.println(context.getProgress());
    }

    /**
     * 自动分页OSS上传
     */
    private static void autoPageOssExport() {
        ProgressbarContext context = new ProgressbarContext();
        ExcelUtil.buildOssDownLoadHandler(() -> {
            //查库
            return new ArrayList<>();
        }, 2000, 50000, ThreadPool.pool, context)
                .setOssAction(inputStream -> {
                    //通过inputStream上传OSS操作
                });
    }

}
