package com.babata.concurrent.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 支持piped管道流异常通信
 * @author zqj
 */
public class PipedInputStreamWithFinalExceptionCheck extends PipedInputStream {

    private final AtomicReference<Exception> exception = new AtomicReference<>(null);
    private final CountDownLatch complete = new CountDownLatch(1);
    private InputStream inputStream;

    public PipedInputStreamWithFinalExceptionCheck(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void close() throws IOException {
        try {
            complete.await();
            final Exception e = exception.get();
            if(e != null) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw  new IOException("interrupted when wait complete");
        } finally {
            inputStream.close();
        }
    }

    /**
     * 通知异常方法
     * @param e
     */
    public void notifyFail(final Exception e) {
        if(e != null) {
            exception.set(e);
        }
    }

    /**
     * 成功完成后提交
     */
    public void complete() {
        complete.countDown();
    }
}
