package com.babata.concurrent.excel.processor;

import com.babata.concurrent.excel.ExcelUtil;
import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.processor.AbstractBatchProcessor;
import com.babata.concurrent.processor.builder.AbstractBatchProcessorBuilder;
import com.babata.concurrent.support.BatchParam;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * http excel导出
 * @author  zqj
 */
public class HttpExcelDownLoadOrderLyProcessor<E extends ExcelExportAble, R extends List<E>> extends ExcelUtil.AbstractExcelDownLoadOrderLyProcessor<E, R> {

    public HttpExcelDownLoadOrderLyProcessor(int batchSize, Supplier<R> select, int rowLimit, boolean orderControl) {
        super(batchSize, select, rowLimit, orderControl);
    }

    public HttpExcelDownLoadOrderLyProcessor(int batchSize, int rowLimit, Supplier<Integer> count, Function<BatchParam, R> customSelect, boolean orderControl) {
        super(batchSize, rowLimit, count, customSelect, orderControl);
    }

    @Override
    protected void packageDownLoad(Function<BiConsumer<Workbook, Integer>, CompletableFuture<Boolean>> doExecute, ExcelUtil.UploadContext uploadContext) {
        //打压缩包导出
        ExcelUtil.compressAbleExport(doExecute, uploadContext, ((uploadContext1, tempDir) -> ExcelUtil.HttpDownCompressExcels(uploadContext.getTableName() + ".zip", tempDir)));
    }

    @Override
    protected void singeDownLoad(Workbook workbook, ExcelUtil.UploadContext uploadContext) {
        //直接写到respose输出流
        ExcelUtil.downloadExcel(uploadContext.getTableName() + ".xlsx", workbook);
    }

    public static class HttpExcelDownLoadOrderLyProcessorBuilder extends AbstractBatchProcessorBuilder<ExcelExportAble> {

        @Override
        public AbstractBatchProcessor newInstance() {
            return new HttpExcelDownLoadOrderLyProcessor(batchSize, rowLimit, count, customSelect, true)
                    .partitionLimit(partitionLimit);
        }
    }
}
