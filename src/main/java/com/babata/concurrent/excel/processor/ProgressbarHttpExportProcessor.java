package com.babata.concurrent.excel.processor;

import com.babata.concurrent.excel.ExcelUtil;
import com.babata.concurrent.excel.context.ProgressbarContext;
import com.babata.concurrent.param.BatchParam;
import com.babata.concurrent.processor.AbstractBatchProcessor;
import com.babata.concurrent.processor.builder.AbstractBatchProcessorBuilder;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 提供查询进度的http下载excel
 * @param <E>
 * @param <R>
 */
public class ProgressbarHttpExportProcessor<E, R extends Collection<E>> extends HttpExcelDownLoadOrderLyProcessor<E, R> {

    private ProgressbarContext progressbarContext;

    private Consumer<ExcelUtil.UploadContext> uploadProgressAction;

    /**
     * 进度条粒度
     */
    private static final Integer DEFAULT_PROCESS_PART= 20;

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

    @Override
    protected ExcelUtil.UploadContext generateContext() {
        return progressbarContext;
    }

    @Override
    protected void prepareWrite(ExcelUtil.UploadContext uploadContext) {
        int partFlag = 1;
        if(getBatchCount() > DEFAULT_PROCESS_PART) {
            //大于进度条粒度的，扩大粒度
            partFlag = getBatchCount()/DEFAULT_PROCESS_PART;
        }
        progressbarContext.setPartFlag(partFlag);
    }

    @Override
    protected void afterWrite(ExcelUtil.UploadContext uploadContext, int taskIndex, ExcelUtil.WorkbookHolder workbookHolder) {
        //如果是分页方式并且本片是倒数第二片，并且已写入完成，则置进度为100，非分片任务按任务index来计算
        boolean canPartitionComplete = false;
        if(partitionAble && (canPartitionComplete = Math.ceil(total*1f/getRowLimit()) - 2 == workbookHolder.getWbIndex()) && workbookHolder.done() || !partitionAble && taskIndex + 1 == getBatchCount()) {
            progressbarContext.setProgress(100);
        } else if(partitionAble || (taskIndex + 1) % progressbarContext.getPartFlag() == 0) {
            //上传进度
            if(partitionAble) {
                if(!canPartitionComplete) {
                    //如果是分片方式，以倒数第二个文件的进度作为整体进度
                    return;
                }
                progressbarContext.setProgress(workbookHolder.calculateProcessRate());
            } else {
                progressbarContext.setProgress((taskIndex + 1)*100/getBatchCount());
            }
        }
        if(uploadProgressAction != null) {
            //用户可指定函数处理进度，如上传redis
            uploadProgressAction.accept(uploadContext);
        }
    }

    public static class ProgressbarHttpExportProcessorBuilder extends AbstractBatchProcessorBuilder {

        private ProgressbarContext progressbarContext;

        public ProgressbarHttpExportProcessorBuilder progressbarContext(ProgressbarContext progressbarContext) {
            this.progressbarContext = progressbarContext;
            return this;
        }

        @Override
        public ProgressbarHttpExportProcessor newInstance() {
            return new ProgressbarHttpExportProcessor(batchSize, rowLimit, count, customSelect, progressbarContext, false);
        }
    }
}
