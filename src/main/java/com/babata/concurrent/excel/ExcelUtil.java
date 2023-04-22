package com.babata.concurrent.excel;

import com.babata.concurrent.excel.context.ProgressbarContext;
import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.excel.processor.HttpExcelDownLoadOrderLyProcessor;
import com.babata.concurrent.excel.processor.OssExcelDownLoadOrderLyProcessor;
import com.babata.concurrent.excel.processor.ProgressbarHttpExportProcessor;
import com.babata.concurrent.excel.resolve.ColumnContext;
import com.babata.concurrent.excel.resolve.ColumnHeadResolve;
import com.babata.concurrent.param.BatchParam;
import com.babata.concurrent.processor.builder.AbstractBatchProcessorBuilder;
import com.babata.concurrent.processor.strategy.BatchDispatchStrategyEnum;
import com.babata.concurrent.processor.strategy.BatchDispatchStrategyProcessor;
import com.babata.concurrent.util.FileUtils;
import com.babata.concurrent.util.HttpContextUtil;
import com.babata.concurrent.util.NumberUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SheetDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * excel工具类
 * @author: zqj
 */
public class ExcelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * SXXSWorkbook指定的在内存中的行数
     */
    public static final Integer ROW_FLUSH_SIZE=1000;

    private static final Map<Class<? extends ExcelExportAble>, Map<Integer,ColumnContext>> headMapCache = new HashMap<>();

    /**
     * 构建http并发下载excel文件处理器（自动并发分页查询，写入是顺序写入）
     * @param select 获取写入excel的数据（注：查询时只对第一个sql分页）
     * @param batchSize 每次查询的数量
     * @param rowLimit 每个sheet页的行数限制（一个文件只支持一个sheet页，多文件则打包成一个zip）
     * @return 处理器实例
     */
    public static  <E extends ExcelExportAble, R extends List<E>> HttpExcelDownLoadOrderLyProcessor<E, R> buildHttpDownLoadHandler(Supplier<R> select, int batchSize, int rowLimit) {
        return new HttpExcelDownLoadOrderLyProcessor<>(batchSize, select, rowLimit, true);
    }

    /**
     * 构建可查询下载进度的http excel导出处理器
     * @param select 获取写入excel的数据（注：查询时只对第一个sql分页）
     * @param batchSize 每次查询的数量
     * @param rowLimit 每个sheet页的行数限制（一个文件只支持一个sheet页，多文件则打包成一个zip）
     * @param uploadContext 上下文，包含下载进度，需要用户new一个放进来
     * @return
     */
    public static  <E extends ExcelExportAble, R extends List<E>> ProgressbarHttpExportProcessor<E, R> buildBarHttpDownLoadHandler(Supplier<R> select, int batchSize, int rowLimit, ProgressbarContext uploadContext) {
        return new ProgressbarHttpExportProcessor<>(batchSize, select, rowLimit, uploadContext, false);
    }

    /**
     * 构建支持OSS上传的excel导出处理器（可查进度，需要用户指定ossAction，提供文件输入流）
     * @param select 获取写入excel的数据（注：查询时只对第一个sql分页）
     * @param batchSize 每次查询的数量
     * @param rowLimit 每个sheet页的行数限制（一个文件只支持一个sheet页，多文件则打包成一个zip）
     * @param e 流转换线程池，内部使用管道流转换IO方向（线程池必须和本excel其他操作相关的线程池完全隔离，否则可能造成管道流死锁，如传入为空，则默认新建线程处理）
     * @param uploadContext 上下文，包含下载进度，需要用户new一个放进来
     * @return
     */
    public static  <E extends ExcelExportAble, R extends List<E>> OssExcelDownLoadOrderLyProcessor<E, R> buildOssDownLoadHandler(Supplier<R> select, int batchSize, int rowLimit, Executor e, ProgressbarContext uploadContext) {
        return new OssExcelDownLoadOrderLyProcessor<>(batchSize, select, rowLimit, e, uploadContext, null, false);
    }

    /**
     * 构建自定义分页的http excel导出处理器（可查进度）
     * @param count 获取总数的函数，分页查询函数需要直接.setSelect传入
     * @param batchSize 每次查询的数量
     * @param rowLimit 每个sheet页的行数限制（一个文件只支持一个sheet页，多文件则打包成一个zip）
     * @param uploadContext 上下文，包含下载进度，需要用户new一个放进来
     * @return
     */
    public static AbstractBatchProcessorBuilder buildSimpleBarHttpDownLoadHandler(Supplier<Integer> count, int batchSize, int rowLimit, ProgressbarContext uploadContext) {
        //return new ProgressbarHttpExportProcessor<>(batchSize, rowLimit, count, uploadContext);
        return new ProgressbarHttpExportProcessor.ProgressbarHttpExportProcessorBuilder()
                .progressbarContext(uploadContext)
                .batchSize(batchSize)
                .rowLimit(rowLimit)
                .count(count);

    }

    /**
     * 构建支持OSS上传的自定义分页excel导出处理器（可查进度，需要用户指定ossAction，提供文件输入流）
     * @param count 获取总数的函数，分页查询函数需要直接.setSelect传入
     * @param batchSize 每次查询的数量
     * @param rowLimit 每个sheet页的行数限制（一个文件只支持一个sheet页，多文件则打包成一个zip）
     * @param e 流转换线程池，内部使用管道流转换IO方向（线程池必须和本excel其他操作相关的线程池完全隔离，否则可能造成管道流死锁，如传入为空，则默认新建线程处理）
     * @param uploadContext 上下文，包含下载进度，需要用户new一个放进来
     * @return
     */
    public static OssExcelDownLoadOrderLyProcessor.OssExcelDownLoadOrderLyProcessorBuilder buildSimpleOssDownLoadProcessorBuilder(Supplier<Integer> count, int batchSize, int rowLimit, Executor e, ProgressbarContext uploadContext) {
        return (OssExcelDownLoadOrderLyProcessor.OssExcelDownLoadOrderLyProcessorBuilder) new OssExcelDownLoadOrderLyProcessor.OssExcelDownLoadOrderLyProcessorBuilder()
                .executorForPipedConvert(e)
                .progressbarContext(uploadContext)
                .batchSize(batchSize)
                .rowLimit(rowLimit)
                .count(count);
    }

    /**
     * 构建自定义分页http并发下载excel文件处理器（自动并发分页查询，写入是顺序写入）
     * @param count 获取总数的函数，分页查询函数需要直接.setSelect传入
     * @param batchSize 每次查询的数量
     * @param rowLimit 每个sheet页的行数限制（一个文件只支持一个sheet页，多文件则打包成一个zip）
     * @return 处理器实例
     */
    public static AbstractBatchProcessorBuilder buildSimpleHttpDownLoadBuilder(Supplier<Integer> count, int batchSize, int rowLimit) {
        return new HttpExcelDownLoadOrderLyProcessor.HttpExcelDownLoadOrderLyProcessorBuilder()
                .batchSize(batchSize)
                .rowLimit(rowLimit)
                .count(count);
    }

    /**
     * 构建自定义分页http下载excel文件处理器（分片下载，多文件并发导出，超大文件导出有显著效率提升）
     * @param count 获取总数的函数，分页查询函数需要直接.setSelect传入
     * @param batchSize 每次查询的数量
     * @param rowLimit 每个sheet页的行数限制（一个文件只支持一个sheet页，多文件则打包成一个zip）
     * @return
     */
    public static  AbstractBatchProcessorBuilder buildPartitionSimpleHttpDownLoadHandler(Supplier<Integer> count, int batchSize, int rowLimit) {
        return new HttpExcelDownLoadOrderLyProcessor.HttpExcelDownLoadOrderLyProcessorBuilder()
                .batchSize(batchSize)
                .rowLimit(rowLimit)
                .count(count)
                .partition(true);
    }

    /**
     * excel导出处理器基类
     * @param <E>
     * @param <R>
     */
    public abstract static class AbstractExcelDownLoadOrderLyProcessor<E, R extends Collection<E>> extends BatchDispatchStrategyProcessor<E, R> {

        private int rowLimit;

        private boolean orderControl;

        public AbstractExcelDownLoadOrderLyProcessor(int batchSize, Supplier<R> select, int rowLimit, boolean orderControl) {
            super(batchSize, select);
            this.rowLimit = rowLimit;
            this.orderControl = orderControl;
            partitionLimit(20);
        }

        public AbstractExcelDownLoadOrderLyProcessor(int batchSize, int rowLimit, Supplier<Integer> count, Function<BatchParam, R> customSelect, boolean orderControl) {
            super(count, customSelect, batchSize, BatchDispatchStrategyEnum.CUSTOM);
            this.rowLimit = rowLimit;
            this.orderControl = orderControl;
            partitionLimit(20);
        }

        public AbstractExcelDownLoadOrderLyProcessor(int batchSize, Supplier<R> select, int rowLimit) {
            super(batchSize, select);
            this.rowLimit = rowLimit;
            partitionLimit(20);
        }

        public AbstractExcelDownLoadOrderLyProcessor(int batchSize, int rowLimit, Supplier<Integer> count, Function<BatchParam, R> customSelect) {
            super(count, customSelect, batchSize, BatchDispatchStrategyEnum.CUSTOM);
            this.rowLimit = rowLimit;
            partitionLimit(20);
        }

        public AbstractExcelDownLoadOrderLyProcessor<E, R> partition(boolean partition) {
            if(partition) {
                partition(rowLimit);
            }
            return this;
        }

        public int getRowLimit() {
            return rowLimit;
        }

        /**
         * 多文件时处理，如打包下载
         * @param doExecute
         * @param uploadContext
         */
        protected abstract void packageDownLoad(Consumer<BiConsumer<Workbook, Integer>> doExecute, UploadContext uploadContext);

        /**
         * 单文件处理
         * @param workbook
         * @param uploadContext
         */
        protected abstract void singeDownLoad(Workbook workbook, UploadContext uploadContext);

        /**
         * 写入excel前置处理
         * @param uploadContext
         */
        protected void prepareWrite(UploadContext uploadContext) {};

        /**
         * 每一个批次数据写入前置处理
         * @param uploadContext
         */
        protected void beforeWrite(UploadContext uploadContext) {};

        /**
         * 每一个批次数据写入后置处理
         * @param uploadContext
         * @param taskIndex
         * @param workbookHolder
         */
        protected void afterWrite(UploadContext uploadContext, int taskIndex, WorkbookHolder workbookHolder) {};

        /**
         * 生成上下文
         * @return
         */
        protected UploadContext generateContext() {
            return new UploadContext();
        }

        @Override
        public void execute(BiConsumer<R, Integer> handler, ThreadPoolExecutor e) {
            UploadContext uploadContext = generateContext();
            WorkbookHolder workbookHolder = new WorkbookHolder();
            prepareWrite(uploadContext);
            Consumer<BiConsumer<Workbook, Integer>> doExecute = doWorkbook -> {
                try {
                    if(partitionAble) {
                        Map<Integer, WorkbookHolder> wbMap = new ConcurrentHashMap<>();
                        super.batchPartitionExecuteOrderly((r, index) -> {
                            beforeWrite(uploadContext);
                            WorkbookHolder workbookHolder0 = writeWorkbook(rowLimit, batchSize, index, wbMap, (List<ExcelExportAble>) r, doWorkbook, uploadContext, getTotal());
                            //下载到本地
                            safeUpload(wbMap, index, doWorkbook, batchSize, rowLimit, orderControl);
                            afterWrite(uploadContext, index, workbookHolder0);
                        }, e);
                    } else {
                        super.batchExecuteOrderly((r, index) -> {
                            beforeWrite(uploadContext);
                            writeWorkbook(rowLimit, workbookHolder, (List<ExcelExportAble>) r, doWorkbook, uploadContext, false);
                            if (index + 1 == getBatchCount() && workbookHolder.workbook != null) {
                                //最后一批数据写入成功
                                doWorkbook.accept(workbookHolder.workbook, uploadContext.fileIndex);
                            }
                            afterWrite(uploadContext, index, workbookHolder);
                        }, e);
                    }
                } catch (InterruptedException interruptedException) {
                    logger.error("线程中断", interruptedException);
                }
            };
            if(getTotal() > rowLimit) {
                //计算文件个数大于1个，需要打包
                packageDownLoad(doExecute, uploadContext);
            } else {
                doExecute.accept((workbook, wbIndex) -> {
                    //单个文件输出
                    singeDownLoad(workbook, uploadContext);
                });
            }
        }
    }

    /**
     * 将数据写入workbook
     * @param rowLimit 每个excel行数限制（一个文件只能一个sheet页）
     * @param workbookHolder 当前workbook持有
     * @param listData 需要写入的数据
     * @param uploadWorkbook 一个文件写入完成后回调
     * @param uploadContext 上下文
     * @param allInMemory 废弃参数，目前全部使用SXXSWorkbook导出
     */
    public static void writeWorkbook(int rowLimit, WorkbookHolder workbookHolder, List<ExcelExportAble> listData, BiConsumer<Workbook, Integer> uploadWorkbook, UploadContext uploadContext, boolean allInMemory){
        //当前workbook
        if(CollectionUtils.isEmpty(listData)) {
            return;
        }
        Map<Integer, ColumnContext> headMap = getHeadMap(listData.get(0));
        if(headMap == null) {
            throw new RuntimeException("headMap not find");
        }
        //当前sheet，如果需要新建sheet则新建后返回
        Sheet sheet = createSheetAndHead(workbookHolder, headMap, uploadWorkbook, uploadContext, rowLimit, allInMemory);
        //设置内容
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 0; i < listData.size(); i++) {
            if(lastRowNum == rowLimit) {
                sheet = createSheetAndHead(workbookHolder, headMap, uploadWorkbook, uploadContext, rowLimit, allInMemory);
                lastRowNum = sheet.getLastRowNum();
            }
            Row row = sheet.createRow(++lastRowNum);
            writeContent(i, row, listData, headMap);
        }
    }

    /**
     * 计算每个文件的完成进度，如完成则回调操作
     * @param wbMap 文件和文件索引的映射
     * @param taskIndex 数据的批次索引
     * @param uploadWorkbook 回调动作，一般是写到本地或上传OSS
     * @param batchSize 一个批次处理的数据数量，即查库每次的条数
     * @param sheetLimit 一个文件即一个sheet页的行数限制
     * @param orderControl 分片数量，大于1说明配置分片，使用分页并发控制，通过taskIndex判断是否写入完成即可
     */
    public static void safeUpload(Map<Integer, WorkbookHolder> wbMap, int taskIndex, BiConsumer<Workbook, Integer> uploadWorkbook, int batchSize, int sheetLimit, boolean orderControl) {
        //计算该任务数据所属的wb下标
        int wbIndex = taskIndex * batchSize / sheetLimit;
        if(orderControl) {
            WorkbookHolder workbookHolder = wbMap.get(wbIndex);
            if((taskIndex + 1) * batchSize % sheetLimit == 0 || taskIndex >= workbookHolder.getTotal()/batchSize) {
                uploadWorkbook.accept(workbookHolder.workbook, wbIndex);
            }
            return;
        }
        boolean canUpload = false;
        Workbook workbook = null;
        synchronized (wbMap) {
            WorkbookHolder workbookHolder = wbMap.get(wbIndex);
            if(workbookHolder == null || workbookHolder.workbook == null) {
                throw new RuntimeException("Workbook index未找到对应的Workbook");
            }
            workbookHolder.markFinish(taskIndex);
            //判断该wb是否全部写完
            if(workbookHolder.done()) {
                canUpload = true;
                workbook = workbookHolder.workbook;
            }
        }
        if(canUpload) {
            uploadWorkbook.accept(workbook, wbIndex);
        }
    }

    /**
     * 多线程写入
     * @param rowLimit
     * @param pageLimit
     * @param taskIndex
     * @param wbMap
     * @param listData
     * @param uploadWorkbook
     * @param uploadContext
     * @param total
     */
    public static WorkbookHolder writeWorkbook(int rowLimit, int pageLimit, int taskIndex, Map<Integer, WorkbookHolder> wbMap, List<ExcelExportAble> listData, BiConsumer<Workbook, Integer> uploadWorkbook, UploadContext uploadContext, int total){
        //根据任务标号index计算所属的Workbook
        int wbIndex = taskIndex * pageLimit / rowLimit;
        Map<Integer, ColumnContext> headMap = getHeadMap(listData.get(0));
        WorkbookHolder workbookHolder = wbMap.computeIfAbsent(wbIndex, key -> new WorkbookHolder(flushWorkBook(null, headMap, null, uploadContext, rowLimit, wbIndex, false), wbIndex, total, rowLimit, pageLimit));
        //当前sheet
        Sheet sheet = workbookHolder.workbook.getSheetAt(0);
        //设置内容
        int cuurentRowNum = taskIndex * pageLimit % rowLimit;
        for (int i = 0; i < listData.size(); i++) {
            //已不需要加锁，分片任务代替并发任务
            Row row = sheet.createRow(++cuurentRowNum);
            writeContent(i, row, listData, headMap);
        }
        return workbookHolder;
    }

    /**
     * 写入值
     * @param index
     * @param row
     * @param listData
     * @param headMap
     */
    private static void writeContent(int index, Row row, List<ExcelExportAble> listData, Map<Integer, ColumnContext> headMap) {
        ExcelExportAble abstractExcelBean = listData.get(index);
        for (int j = 0; j < headMap.size() - 1; j++) {
            Cell cell = row.createCell(j);
            ColumnContext column = headMap.get(j);
            cell.setCellValue(column.getFun().apply(abstractExcelBean));
        }
    }

    /**
     * 刷新workbook（如行数达到限制后新建）
     * @param workbook
     * @param headMap
     * @param uploadWorkbook
     * @param uploadContext
     * @param rowLimit
     * @param wbIndex
     * @param allInMemory
     * @return
     */
    private static Workbook flushWorkBook(Workbook workbook, Map<Integer, ColumnContext> headMap, BiConsumer<Workbook, Integer> uploadWorkbook, UploadContext uploadContext, int rowLimit, Integer wbIndex, boolean allInMemory) {
        if (workbook == null || workbook.getSheetAt(0).getLastRowNum() == rowLimit) {
            if(workbook != null && uploadWorkbook != null) {
                //文件已写满，需要直接上传上传
                uploadWorkbook.accept(workbook, wbIndex == null?uploadContext.fileIndex++:wbIndex);
            }
            uploadContext.tableName = headMap.get(-1).getName();
            if(allInMemory) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new SXSSFWorkbook(ROW_FLUSH_SIZE);
            }
            Sheet sheet=workbook.createSheet("sheet" + 0);
            //设置标题
            CellStyle style = createStyles(workbook);
            Row titleRow = sheet.createRow(0);
            int titleSize = headMap.size()- 1;
            for (int i = 0; i < titleSize; i++) {
                Cell cell = titleRow.createCell(i);
                cell.setCellStyle(style);
                ColumnContext column = headMap.get(i);
                cell.setCellValue(column.getName());
            }
        }
        return workbook;
    }

    /**
     * 可能创建worknook并填充表头
     * @param workbookHolder
     * @param headMap
     * @param uploadWorkbook
     * @param uploadContext
     * @param rowLimit
     * @param allInMemory
     * @return
     */
    private static Sheet createSheetAndHead(WorkbookHolder workbookHolder, Map<Integer, ColumnContext> headMap, BiConsumer<Workbook, Integer> uploadWorkbook, UploadContext uploadContext, int rowLimit, boolean allInMemory) {
        synchronized (workbookHolder) {
            workbookHolder.workbook = flushWorkBook(workbookHolder.workbook, headMap, uploadWorkbook, uploadContext, rowLimit, null, allInMemory);
            return workbookHolder.workbook.getSheetAt(0);
        }
    }

    /**
     * 设置列格式
     * @param workbook
     * @return
     */
    private static CellStyle createStyles(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        //水平居中
        style.setAlignment(HorizontalAlignment.CENTER);
        //垂直居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /**
     * 获取列名和列值转换配置
     * @param excelBean
     * @return
     */
    private static Map<Integer, ColumnContext> getHeadMap(ExcelExportAble excelBean) {
        if(excelBean == null) {
            throw new RuntimeException("excelBean is null");
        }
        Class<? extends ExcelExportAble> clazz = excelBean.getClass();
        Map<Integer, ColumnContext> headMap = headMapCache.get(clazz);
        if(headMap == null) {
            synchronized (headMapCache) {
                headMap = headMapCache.computeIfAbsent(clazz, clazz0 -> ColumnHeadResolve.buildHeadMap(clazz0));
            }
        }
        return headMap;
    }

    /**
     * 压缩打包导出
     * @param doExecute
     * @param uploadContext
     * @param doDownOrUpload
     */
    public static void compressAbleExport(Consumer<BiConsumer<Workbook, Integer>> doExecute, UploadContext uploadContext, BiConsumer<UploadContext, String> doDownOrUpload) {
        String tempDir = UUID.randomUUID().toString();
        File file = null;
        try {
            file = new File(tempDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            BiConsumer<Workbook, Integer> uploadWorkbook = (workbook, wbIndex) -> {
                try(BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempDir + "/" + uploadContext.tableName + wbIndex + ".xlsx"))) {
                    workbook.write(outputStream);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                } finally {
                    closeWorkbook(workbook);
                }
            };
            doExecute.accept(uploadWorkbook);
            doDownOrUpload.accept(uploadContext, tempDir);
        } finally {
            if(file.exists()) {
                FileUtils.deleteFile(file);
            }
        }
    }

    /**
     * http下载压缩包
     * @param fileName
     * @param sourceFilePath
     */
    public static void HttpDownCompressExcels(String fileName, String sourceFilePath) {
        HttpServletResponse response= HttpContextUtil.getResponse();
        OutputStream outputStream=null;
        try {
            setResponse(response,fileName);
            outputStream = response.getOutputStream();
            FileUtils.compressToZip(sourceFilePath, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("导出excel失败", e);
        }finally {
            //释放资源
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * http下载单个excel文件
     * @param fileName
     * @param workbook
     */
    public static void downloadExcel(String fileName,Workbook workbook){
        HttpServletResponse response= HttpContextUtil.getResponse();
        OutputStream outputStream=null;
        try {
            setResponse(response,fileName);
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("导出excel失败", e);
        }finally {
            //释放资源
            IOUtils.closeQuietly(outputStream);
            closeWorkbook(workbook);
        }
    }

    private static void closeWorkbook(Workbook workbook) {
        if(workbook != null) {
            try {
                workbook.close();
                deleteSXSSFTempFiles(workbook);
            } catch (IOException e) {
                logger.error("error close fail", e);
            }
        }
    }

    private static void setResponse(HttpServletResponse response,String fileName) {
        //向浏览器发送一个响应头，设置浏览器的解码方式为UTF-8
        response.reset();
        response.setHeader("Content-type","text/html;charset=UTF-8");
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename="+ new String(fileName.getBytes(StandardCharsets.UTF_8),StandardCharsets.ISO_8859_1));
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
    }

    public static class WorkbookHolder {
        //标志位最大长度
        public static final int MAX_LEFT_SHIFT = 32;
        //最大标志数，即对应二进制32个1
        public static final int MAX_WAIT = -1;
        //标志位掩码
        public static final int WAIT_MASK = MAX_LEFT_SHIFT - 1;
        //标志位图
        int[] waitBits;
        private Workbook workbook;
        //一个sheet页可容纳最大的批次数
        private int sheetBatchCount;
        //本sheet设定的批次数
        private int thisSheetBatchCount;
        //文件index
        private int wbIndex;
        //数据总数
        private int total;

        public WorkbookHolder() {}

        public WorkbookHolder(Workbook workbook, int wbIndex, int total, int sheetLimit, int batchSize) {
            this.workbook = workbook;
            this.wbIndex = wbIndex;
            this.total = total;
            this.sheetBatchCount = (int) Math.ceil(sheetLimit*1f/batchSize);
            if(wbIndex < total/sheetLimit) {
                calculateWaitBit(0, batchSize, sheetLimit);
            } else {
                calculateWaitBit(total % sheetLimit, batchSize, sheetLimit);
            }
        }

        /**
         * 计算并发任务进度位图
         * @param surplus
         * @param batchSize
         * @param sheetLimit
         */
        private void calculateWaitBit(int surplus, int batchSize, int sheetLimit) {
            int[] waitBits;
            int leftShift = (int) Math.ceil((surplus==0?sheetLimit:surplus)*1f/batchSize);
            if(batchSize * MAX_LEFT_SHIFT >= sheetLimit) {
                waitBits = new int[1];
                waitBits[0] = (1 << leftShift) - 1;
                thisSheetBatchCount = leftShift;
            } else {
                int waitBitCount = (int) Math.ceil(leftShift*1f/MAX_LEFT_SHIFT);
                waitBits = new int[waitBitCount];
                for (int i = 0; i < waitBitCount - 1; i++) {
                    waitBits[i] = MAX_WAIT;
                }
                waitBits[waitBitCount - 1] = (1 << leftShift % MAX_LEFT_SHIFT) - 1;
                thisSheetBatchCount = (waitBitCount - 1) * MAX_LEFT_SHIFT + leftShift % MAX_LEFT_SHIFT;
            }
            this.waitBits = waitBits;
        }

        /**
         * 标记该任务为完成状态
         * @param index
         */
        public void markFinish(int index) {
            int indexInWaitBit = index - wbIndex * sheetBatchCount;
            int waitBitsIndex = indexInWaitBit/MAX_LEFT_SHIFT;
            waitBits[waitBitsIndex] = waitBits[waitBitsIndex] ^ (1 << (indexInWaitBit & WAIT_MASK));
        }

        /**
         * 计算当前任务进度
         * @return
         */
        public int calculateProcessRate() {
            int[] waitBits0 = waitBits;
            int thisSheetBatchCount0 = thisSheetBatchCount;
            int wait = 0;
            for (int i = 0; i < waitBits0.length; i++) {
                wait += NumberUtil.hammingWeight(waitBits0[i]);
            }
            return (thisSheetBatchCount0 - wait)*100/thisSheetBatchCount0;
        }

        /**
         * 该片是否全部写完
         * @return
         */
        public boolean done() {
            int[] waitBits0 = waitBits;
            for (int i = 0; i < waitBits0.length; i++) {
                if(waitBits0[i] != 0) {
                    return false;
                }
            }
            return true;
        }

        public int getWbIndex() {
            return wbIndex;
        }

        public int getTotal() {
            return total;
        }
    }

    public static class UploadContext implements Serializable {
        private static final long serialVersionUID = 5943941474185938252L;

        /**
         * 文件序号
         */
        private int fileIndex;

        /**
         * 表名
         */
        private transient String tableName;

        /**
         * 错误描述
         */
        private String errorMsg;

        public int getFileIndex() {
            return fileIndex;
        }

        public String getTableName() {
            return tableName;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }

    /**
     * 删除SXSS的临时文件
     * @param workbook
     */
    public static void deleteSXSSFTempFiles(Workbook workbook) {

        int numberOfSheets = workbook.getNumberOfSheets();
        // 遍历所有sheet临时文件
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheetAt = workbook.getSheetAt(i);
            // 仅当工作表由流写入时才删除
            if (sheetAt instanceof SXSSFSheet) {
                File f = null;
                try {
                    SheetDataWriter sdw = (SheetDataWriter) getPrivateAttribute(sheetAt, "_writer");
                    f = (File) getPrivateAttribute(sdw, "_fd");
                } catch (NoSuchFieldException e) {
                    logger.error("close error", e);
                } catch (IllegalAccessException e) {
                    logger.error("close error", e);
                }
                try {
                    f.delete();
                } catch (Exception ex) {
                    // 无法删除文件
                }
            }
        }
    }

    /**
     * 文件权限
     * @param containingClass
     * @param fieldToGet
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Object getPrivateAttribute(Object containingClass, String fieldToGet) throws NoSuchFieldException, IllegalAccessException {
        // 获取containingClass实例的字段
        Field declaredField = containingClass.getClass().getDeclaredField(fieldToGet);
        // 将其设置为可访问
        declaredField.setAccessible(true);
        // 访问它
        Object get = declaredField.get(containingClass);
        return get;

    }

}
