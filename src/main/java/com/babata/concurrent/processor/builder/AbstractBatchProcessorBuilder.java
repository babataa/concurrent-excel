package com.babata.concurrent.processor.builder;

import com.babata.concurrent.support.BatchParam;
import com.babata.concurrent.processor.AbstractBatchProcessor;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * BatchProcessor构造器
 * @author zqj
 */
public abstract class AbstractBatchProcessorBuilder<E> {
    protected int batchSize;
    protected int rowLimit;
    protected Supplier<Collection<? extends E>> select;
    protected Supplier<Integer> count;
    protected Function<BatchParam, Collection<? extends E>> customSelect;
    protected boolean partition;
    protected int partitionLimit;

    public AbstractBatchProcessorBuilder<E> batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public AbstractBatchProcessorBuilder<E> rowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
        return this;
    }

    public AbstractBatchProcessorBuilder<E> select(Supplier<Collection<? extends E>> select) {
        this.select = select;
        return this;
    }

    public AbstractBatchProcessorBuilder<E> count(Supplier<Integer> count) {
        this.count = count;
        return this;
    }

    public AbstractBatchProcessorBuilder<E> partition(boolean partition) {
        this.partition = partition;
        return this;
    }

    public AbstractBatchProcessorBuilder<E> partitionLimit(int partitionLimit) {
        this.partitionLimit = partitionLimit;
        return this;
    }

    public AbstractBatchProcessorBuilder<E> customSelect(Function<BatchParam, Collection<? extends E>> customSelect) {
        this.customSelect = customSelect;
        return this;
    }

    public AbstractBatchProcessor<? extends E, Collection<? extends E>> build() {
        AbstractBatchProcessor<? extends E, Collection<? extends E>> processor = newInstance();
        if(partition) {
            processor.partition(rowLimit);
        }
        return processor;
    }

    public abstract AbstractBatchProcessor<? extends E, Collection<? extends E>> newInstance();

}
