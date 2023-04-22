package com.babata.concurrent.excel.processor;

import com.babata.concurrent.excel.ExcelUtil;
import com.babata.concurrent.excel.context.ProgressbarContext;
import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.param.BatchParam;
import com.babata.concurrent.processor.AbstractBatchProcessor;
import com.babata.concurrent.processor.builder.AbstractBatchProcessorBuilder;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 提供查询进度的http下载excel
 * @author  zqj
 */
public class ProgressbarHttpExportProcessor<E, R extends Collection<E>> extends HttpExcelDownLoadOrderLyProcessor<E, R> {

    private ProgressbarContext progressbarContext;

    private Consumer<ExcelUtil.UploadContext> uploadProgressAction;

    /**
     * 进度条粒度
     */
    private int processPart= 20;

    public ProgressbarHttpExportProcessor(int batchSize, Supplier<R> select, int rowLimit, ProgressbarContext progressbarContext, boolean orderControl) {
        super(batchSize, select, rowLimit, orderControl);
        this.progressbarContext = progressbarContext;
    }

    public ProgressbarHttpExportProcessor(int batchSize, int rowLimit, Supplier<Integer> count, Function<BatchParam, R> customSelect, ProgressbarContext progressbarContext, boolean orderControl) {
        super(batchSize, rowLimit, count, customSelect, orderControl);
        this.progressbarContext = progressbarContext;
    }

    public ProgressbarHttpExportProcessor setUploadWorkbook(Consumer<ExcelUtil.UploadContext> uploadProgressAction) {
        this.uploadProgressAction = uploadProgressAction;
        return this;
    }

    public ProgressbarHttpExportProcessor processPart(int processPart) {
        this.processPart = processPart;
        return this;
    }

    @Override
    protected ExcelUtil.UploadContext generateContext() {
        return progressbarContext;
    }

    @Override
    protected void prepareWrite(ExcelUtil.UploadContext uploadContext) {
        int partFlag = 1;
        if(getBatchCount() > processPart) {
            //大于进度条粒度的，扩大粒度
            partFlag = getBatchCount()/processPart;
        }
        progressbarContext.setPartFlag(partFlag);
    }

    @Override
    protected void afterWrite(ExcelUtil.UploadContext uploadContext, int taskIndex, ExcelUtil.WorkbookHolder workbookHolder) {
        ProgressbarContext context = progressbarContext;
        int batchSize0 = batchSize;
        if((taskIndex + 1) * batchSize0 % getRowLimit() == 0 || taskIndex >= total/batchSize0) {
            int finishFileCount = context.getFinishFileCount().incrementAndGet();
            if(finishFileCount == partition) {
                context.setProgress(100);
            }
        }
        int finishCount = context.getFinishCount().incrementAndGet();
        if (finishCount % context.getPartFlag() == 0) {
            //上传进度
            context.setProgress(finishCount*100/getBatchCount());
            if(uploadProgressAction != null) {
                //用户可指定函数处理进度，如上传redis
                uploadProgressAction.accept(uploadContext);
            }
        }
    }

    public static class ProgressbarHttpExportProcessorBuilder extends AbstractBatchProcessorBuilder<ExcelExportAble> {

        protected ProgressbarContext progressbarContext;

        protected int processPart = 20;

        public ProgressbarHttpExportProcessorBuilder progressbarContext(ProgressbarContext progressbarContext) {
            this.progressbarContext = progressbarContext;
            return this;
        }

        public ProgressbarHttpExportProcessorBuilder processPart(int processPart) {
            this.processPart = processPart;
            return this;
        }

        @Override
        public ProgressbarHttpExportProcessor newInstance() {
            return (ProgressbarHttpExportProcessor) new ProgressbarHttpExportProcessor(batchSize, rowLimit, count, customSelect, progressbarContext, true)
                    .processPart(processPart)
                    .partitionLimit(partitionLimit);
        }
    }
}
