package com.babata.concurrent.processor.strategy;

import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.excel.processor.HttpExcelDownLoadOrderLyProcessor;
import com.babata.concurrent.param.BatchParam;
import com.babata.concurrent.processor.AbstractBatchProcessor;
import com.babata.concurrent.processor.builder.AbstractBatchProcessorBuilder;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 分批策略实现
 * @author  zqj
 */
public class BatchDispatchStrategyProcessor<E, R extends Collection<E>> extends AbstractBatchProcessor<E, R> {

    private BatchDispatchStrategyEnum batchStrategyEnum = BatchDispatchStrategyEnum.AUTO_PAGE;

    private Function<BatchParam, R> customSelect;

    private boolean initFlag = false;

    protected BatchDispatchStrategyProcessor(int batchSize, Supplier<R> select) {
        super(batchSize, select);
        delayInit();
    }

    protected BatchDispatchStrategyProcessor(Supplier<Integer> count, Function<BatchParam, R> customSelect, int batchSize) {
        super(count, batchSize);
        this.batchStrategyEnum = BatchDispatchStrategyEnum.CUSTOM;
        this.customSelect = customSelect;
        delayInit();
    }

    private void delayInit() {
        initFlag = true;
        init();
    }

    @Override
    protected void init() {
        if(!initFlag) {
            return;
        }
        if(BatchDispatchStrategyEnum.AUTO_PAGE == batchStrategyEnum) {
            Page<?> page = PageHelper.startPage(1, -1, true);
            select.get();
            total = (int) page.getTotal();
        } else {
            total = count.get();
        }
        batchCount = (int) Math.ceil(total*1f/batchSize);
    }

    @Override
    protected R takeTask(int index) {
        int batchSize0 = batchSize;
        if(BatchDispatchStrategyEnum.AUTO_PAGE == batchStrategyEnum) {
            PageHelper.startPage(index, batchSize0, false);
            return reryAbleSelect(0);
        }
        BatchParam batchParam = new BatchParam();
        batchParam.setBatchStart((index - 1) * batchSize0);
        batchParam.setBatchSize(batchSize0);
        batchParam.setBatchIndex(index);
        return customSelect.apply(batchParam);
    }

    public static class BatchDispatchStrategyProcessorBuilder<E> extends AbstractBatchProcessorBuilder<E> {

        @Override
        public AbstractBatchProcessor newInstance() {
            return new BatchDispatchStrategyProcessor(count, customSelect, batchSize)
                    .partitionLimit(partitionLimit);
        }
    }
}
