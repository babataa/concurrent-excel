package com.babata.concurrent.excel.processor;

import com.babata.concurrent.excel.ExcelUtil;
import com.babata.concurrent.excel.context.ProgressbarContext;
import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.support.BatchParam;
import com.babata.concurrent.support.io.PipedInputStreamWithFinalExceptionCheck;
import com.babata.concurrent.support.util.IoUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * OSS excel导出
 * @author  zqj
 */
public class OssExcelDownLoadOrderLyProcessor<E extends ExcelExportAble, R extends List<E>> extends ProgressbarHttpExportProcessor<E, R> {

    /**
     * 用于管道流输出转输入流，然后直接上传oss
     */
    private Executor executorForPipedConvert;

    private Consumer<InputStream> ossAction;

    public OssExcelDownLoadOrderLyProcessor(int batchSize, Supplier<R> select, int rowLimit, Executor executorForPipedConvert, ProgressbarContext progressbarContext, Consumer<InputStream> ossAction, boolean orderControl) {
        super(batchSize, select, rowLimit, progressbarContext, orderControl);
        this.executorForPipedConvert = executorForPipedConvert;
        this.ossAction = ossAction;
    }

    public OssExcelDownLoadOrderLyProcessor(int batchSize, int rowLimit, Supplier<Integer> count, Function<BatchParam, R> customSelect, Executor executorForPipedConvert, ProgressbarContext progressbarContext, Consumer<InputStream> ossAction, boolean orderControl) {
        super(batchSize, rowLimit, count, customSelect, progressbarContext, orderControl);
        this.executorForPipedConvert = executorForPipedConvert;
        this.ossAction = ossAction;
    }

    public void setOssAction(Consumer<InputStream> ossAction) {
        this.ossAction = ossAction;
    }

    @Override
    protected void packageDownLoad(Function<BiConsumer<Workbook, Integer>, CompletableFuture<Boolean>> doExecute, ExcelUtil.UploadContext uploadContext) {
        ExcelUtil.compressAbleExport(doExecute, uploadContext, ((uploadContext1, tempDir) -> {
            //转换成输入流，供用户操作
            InputStream inputStream = IoUtil.ossDownCompressExcels(tempDir, executorForPipedConvert);
            //用户指定的通过输入流上传
            ossAction.accept(inputStream);
        }));
    }

    @Override
    protected void singeDownLoad(Workbook workbook, ExcelUtil.UploadContext uploadContext) {
        try {
            InputStream inputStream = IoUtil.pipedStreamConvertForWorkbook(workbook, executorForPipedConvert);
            ossAction.accept(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    public static class OssExcelDownLoadOrderLyProcessorBuilder extends ProgressbarHttpExportProcessorBuilder {

        private Executor executorForPipedConvert;

        private Consumer<PipedInputStreamWithFinalExceptionCheck> ossAction;

        public OssExcelDownLoadOrderLyProcessorBuilder executorForPipedConvert(Executor executorForPipedConvert) {
            this.executorForPipedConvert = executorForPipedConvert;
            return this;
        }

        public OssExcelDownLoadOrderLyProcessorBuilder ossAction(Consumer<PipedInputStreamWithFinalExceptionCheck> ossAction) {
            this.ossAction = ossAction;
            return this;
        }

        @Override
        public OssExcelDownLoadOrderLyProcessor newInstance() {
            return (OssExcelDownLoadOrderLyProcessor) new OssExcelDownLoadOrderLyProcessor(batchSize, rowLimit, count, customSelect, executorForPipedConvert, progressbarContext, ossAction, true)
                    .processPart(processPart)
                    .partitionLimit(partitionLimit);
        }
    }
}
