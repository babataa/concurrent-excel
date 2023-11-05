package com.babata.concurrent.support.util;

import com.babata.concurrent.support.io.PipedInputStreamWithFinalExceptionCheck;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @description:
 * @author: zqj
 */
public class FileUtils {

    /**
     * 压缩文件
     *
     * @param sourceFilePath 源文件路径
     * @param zipFilename    压缩文件名
     */
    public static File compressToZip(String sourceFilePath, String zipFilename) {
        File sourceFile = new File(sourceFilePath);
        File zipFile = new File(zipFilename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            writeZip(sourceFile, "", zos);
            return zipFile;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        } finally {
            deleteFile(sourceFile);
        }
    }

    /**
     * 压缩文件
     * @param sourceFilePath 源文件路径
     * @param outputStream
     */
    public static void compressToZip(String sourceFilePath, OutputStream outputStream) {
        File sourceFile = new File(sourceFilePath);
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            writeZip(sourceFile, "", zos);
            //文件压缩完成后，删除被压缩文件
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        } finally {
            deleteFile(sourceFile);
        }
    }

    /**
     * 遍历所有文件，压缩
     *
     * @param file       源文件目录
     * @param parentPath 压缩文件目录
     * @param zos        文件流
     */
    public static void writeZip(File file, String parentPath, ZipOutputStream zos) {
        if (file.isDirectory()) {
            //目录
            parentPath += file.getName() + File.separator;
            File[] files = file.listFiles();
            for (File f : files) {
                writeZip(f, parentPath, zos);
            }
        } else {
            //文件
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                //指定zip文件夹
                //ZipEntry zipEntry = new ZipEntry(parentPath + file.getName());
                //生成的zip不包含该文件夹
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                int len;
                byte[] buffer = new byte[1024 * 10];
                while ((len = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, len);
                    zos.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        }
    }

    /**
     * 删除文件夹
     *
     * @param file
     * @return
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            //判断是否为文件，是，则删除
            file.delete();
        }
        else {
            //不为文件，则为文件夹
            String[] childFilePath = file.list();//获取文件夹下所有文件相对路径
            for (String path:childFilePath) {
                deleteFile(new File(file.getAbsoluteFile()+"/"+path));//递归，对每个都进行判断
            }
            // 如果不保留文件夹本身 则执行此行代码
            file.delete();
        }
    }

    /**
     * 管道通讯流写文件到本地
     * @param inputStream
     * @param fileName
     * @throws IOException
     */
    public static void uploadByPipedStream(PipedInputStreamWithFinalExceptionCheck inputStream, String fileName) throws IOException {
        try(BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(fileName))) {
            int len=-1;
            byte[] b=new byte[1024];
            while((len=inputStream.read(b))!=-1){
                out.write(b,0,len);
            }
        } catch (Throwable throwable){
            inputStream.notifyFail(new RuntimeException(throwable));
        } finally {
            inputStream.close();
        }

    }
}
