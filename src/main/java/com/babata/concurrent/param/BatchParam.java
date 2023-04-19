package com.babata.concurrent.param;

/**
 * @description:
 * @author: zqj
 */
public class BatchParam {
    private int batchIndex;
    private int batchStart;
    private int batchSize;

    public int getBatchStart() {
        return batchStart;
    }

    public void setBatchStart(int batchStart) {
        this.batchStart = batchStart;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchIndex() {
        return batchIndex;
    }

    public void setBatchIndex(int batchIndex) {
        this.batchIndex = batchIndex;
    }
}
