package com.babata;

import com.babata.concurrent.excel.ExcelUtil;
import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.param.BatchParam;
import com.babata.concurrent.processor.AbstractBatchProcessor;
import com.babata.concurrent.processor.builder.AbstractBatchProcessorBuilder;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * excel导出到本地测试
 * @author zqj
 */
public class LocalExcelTest {

    public static class TestExcelDownLoadOrderLyProcessor<E, R extends Collection<E>> extends ExcelUtil.AbstractExcelDownLoadOrderLyProcessor<E, R> {
        public TestExcelDownLoadOrderLyProcessor(int batchSize, Supplier<R> select, int rowLimit, boolean orderControl) {
            super(batchSize, select, rowLimit, orderControl);
        }

        public TestExcelDownLoadOrderLyProcessor(int batchSize, int rowLimit, Supplier<Integer> count, Function<BatchParam, R> customSelect, boolean orderControl) {
            super(batchSize, rowLimit, count, customSelect, orderControl);
        }

        @Override
        protected void packageDownLoad(Consumer<BiConsumer<Workbook, Integer>> doExecute, ExcelUtil.UploadContext uploadContext) {
            doExecute.accept((workbook, index)  -> {
                try {
                    OutputStream outputStream = new FileOutputStream(uploadContext.getTableName() + index + ".xlsx");
                    workbook.write(outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    ExcelUtil.deleteSXSSFTempFiles(workbook);
                }
            });
        }

        @Override
        protected void singeDownLoad(Workbook workbook, ExcelUtil.UploadContext uploadContext) {
            try {
                OutputStream outputStream = new FileOutputStream(uploadContext.getTableName() + ".xlsx");
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ExcelUtil.deleteSXSSFTempFiles(workbook);
            }
        }

        public static class TestExcelDownLoadOrderLyProcessorBuilder extends AbstractBatchProcessorBuilder<ExcelExportAble> {

            @Override
            public AbstractBatchProcessor newInstance() {
                return new TestExcelDownLoadOrderLyProcessor(batchSize, rowLimit, count, customSelect, true);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int count = 1000001;
        long start = System.currentTimeMillis();
        new TestExcelDownLoadOrderLyProcessor.TestExcelDownLoadOrderLyProcessorBuilder()
                .batchSize(2000)
                .rowLimit(50000)
                //获取总数的方法
                .count(() -> count)
                //自定义分页获取数据的方法
                .customSelect(batchParam -> DataFactory.get(batchParam, count))
                //开启分片下载，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
                .partition(true)
                //同时限制10个文件并发写入
                .partitionLimit(10)
                .build()
                .addExceptionHandler(exception -> {
                    //异常处理
                })
                .execute(null, ThreadPool.pool);
        System.out.println(System.currentTimeMillis() - start);
    }

}
