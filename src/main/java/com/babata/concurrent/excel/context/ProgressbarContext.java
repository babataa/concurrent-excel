package com.babata.concurrent.excel.context;

import com.babata.concurrent.excel.ExcelUtil;

/**
 * 进度条context
 * @author  zqj
 */
public class ProgressbarContext extends ExcelUtil.UploadContext {
    private static final long serialVersionUID = 5909941474175938452L;
    /**
     * 进度条
     */
    private volatile int progress;

    /**
     * 进度条粒度
     */
    private transient int partFlag;

    public int getProgress() {
        return progress;
    }

    public int getPartFlag() {
        return partFlag;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setPartFlag(int partFlag) {
        this.partFlag = partFlag;
    }
}
