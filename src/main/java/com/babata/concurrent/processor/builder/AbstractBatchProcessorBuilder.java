package com.babata.concurrent.processor.builder;

import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.excel.processor.HttpExcelDownLoadOrderLyProcessor;
import com.babata.concurrent.param.BatchParam;
import com.babata.concurrent.processor.AbstractBatchProcessor;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * BatchProcessor构造器
 * @author zqj
 */
public abstract class AbstractBatchProcessorBuilder {
    protected int batchSize;
    protected int rowLimit;
    protected Supplier<Collection<? extends ExcelExportAble>> select;
    protected Supplier<Integer> count;
    protected Function<BatchParam, Collection<? extends ExcelExportAble>> customSelect;
    protected boolean partition;
    protected int partitionLimit;

    public AbstractBatchProcessorBuilder batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public AbstractBatchProcessorBuilder rowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
        return this;
    }

    public AbstractBatchProcessorBuilder select(Supplier<Collection<? extends ExcelExportAble>> select) {
        this.select = select;
        return this;
    }

    public AbstractBatchProcessorBuilder count(Supplier<Integer> count) {
        this.count = count;
        return this;
    }

    public AbstractBatchProcessorBuilder partition(boolean partition) {
        this.partition = partition;
        return this;
    }

    public AbstractBatchProcessorBuilder partitionLimit(int partitionLimit) {
        this.partitionLimit = partitionLimit;
        return this;
    }

    public AbstractBatchProcessorBuilder customSelect(Function<BatchParam, Collection<? extends ExcelExportAble>> customSelect) {
        this.customSelect = customSelect;
        return this;
    }

    public AbstractBatchProcessor<? extends ExcelExportAble, Collection<? extends ExcelExportAble>> build() {
        AbstractBatchProcessor<? extends ExcelExportAble, Collection<? extends ExcelExportAble>> processor = newInstance();
        if(partition) {
            processor.partition(rowLimit);
        }
        return processor;
    }

    public abstract AbstractBatchProcessor<? extends ExcelExportAble, Collection<? extends ExcelExportAble>> newInstance();

}
