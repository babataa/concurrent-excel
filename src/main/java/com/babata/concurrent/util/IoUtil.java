package com.babata.concurrent.util;

import com.babata.concurrent.io.PipedInputStreamWithFinalExceptionCheck;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * excel导出模板接口
 * @author  zqj
 */
public class IoUtil {

    /**
     * 压缩后上传OSS
     * @param sourceFilePath
     * @param e
     * @return
     */
    public static InputStream ossDownCompressExcels(String sourceFilePath, Executor e) {
        PipedOutputStream outputStream = new PipedOutputStream();
        try {
            return IoUtil.pipedStreamConvertForPackage(sourceFilePath, outputStream, e);
        } catch (IOException ex) {
            throw new RuntimeException("convert io error", ex);
        }
    }

    /**
     * 对本地文件夹打包并转换成输入流
     * @param sourceFilePath
     * @param outputStream
     * @param e
     * @return
     * @throws IOException
     */
    public static InputStream pipedStreamConvertForPackage(String sourceFilePath, PipedOutputStream outputStream, Executor e) throws IOException {
        PipedInputStreamWithFinalExceptionCheck inputStream = new PipedInputStreamWithFinalExceptionCheck(new PipedInputStream());
        outputStream.connect(inputStream);
        if(e == null) {
            new Thread(() -> {
                doConvert(sourceFilePath, outputStream, inputStream);
            }).start();
        } else {
            e.execute(() -> {
                doConvert(sourceFilePath, outputStream, inputStream);
            });
        }
        return inputStream;
    }

    /**
     * 直接将workbook转换成输入流
     * @param workbook
     * @param e
     * @return
     * @throws IOException
     */
    public static InputStream pipedStreamConvertForWorkbook(Workbook workbook, Executor e) throws IOException {
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStreamWithFinalExceptionCheck inputStream = new PipedInputStreamWithFinalExceptionCheck(new PipedInputStream());
        outputStream.connect(inputStream);
        if(e == null) {
            new Thread(() -> {
                try {
                    doConvert(workbook, outputStream, inputStream);
                } catch (IOException ex) {
                    throw new RuntimeException("IO convert error", ex);
                }
            }).start();
        } else {
            e.execute(() -> {
                try {
                    doConvert(workbook, outputStream, inputStream);
                } catch (IOException ex) {
                    throw new RuntimeException("IO convert error", ex);
                }
            });
        }
        return inputStream;
    }

    public static void doConvert(String sourceFilePath, PipedOutputStream outputStream, PipedInputStreamWithFinalExceptionCheck inputStream) {
        try {
            FileUtils.compressToZip(sourceFilePath, outputStream);
        } catch (Exception ex) {
            inputStream.notifyFail(ex);
            throw ex;
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                inputStream.complete();
            }
        }
    }

    public static void doConvert(Workbook workbook, PipedOutputStream outputStream, PipedInputStreamWithFinalExceptionCheck inputStream) throws IOException {
        try {
            workbook.write(outputStream);
        } catch (Exception ex) {
            inputStream.notifyFail(ex);
            throw ex;
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                inputStream.complete();
            }
        }
    }

}
