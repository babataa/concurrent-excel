package com.babata.concurrent.processor;

import com.babata.concurrent.exception.BatchHandleException;
import com.babata.concurrent.util.NumberUtil;
import com.babata.concurrent.util.PartitionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 分批处理工具
 * @author zqj
 */
public abstract class AbstractBatchProcessor<E, R extends Collection<E>> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBatchProcessor.class);
    protected int batchSize;
    //每次分批处理获取数据的方法
    protected Supplier<R> select;
    private int pageNum = 1;
    protected int batchCount;
    //数据总数
    protected int total;
    //异常回调
    private List<Consumer<Exception>> exceptionHandlers = new ArrayList<>();
    //任务取消标识
    private AtomicBoolean cancel = new AtomicBoolean(false);
    private static Unsafe UNSAFE;
    //线程数组元素偏移量
    private static final int REF_ELEMENT_SHIFT;
    //线程数组第一个元素起始偏移量
    private static final long REF_ARRAY_BASE;
    //线程下标掩码
    private long indexMask;
    //当前最新已完成的批次索引
    private volatile int pos = -1;
    //重试次数
    private int maxRetryTimes = 3;
    boolean useCAS = true;

    protected Supplier<Integer> count;
    //分片数量
    protected int partition = 1;

    private int partitionSize = 0;

    protected boolean partitionAble = false;
    //并发分片限制，默认-1无限制
    protected int partitionLimit = -1;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE =  (Unsafe) theUnsafe.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("unsafe init error");
        }
        //线程数组每个元素大小
        final int scale = UNSAFE.arrayIndexScale(Thread[].class);
        if (4 == scale) {
            //32位占4个字节
            REF_ELEMENT_SHIFT = 2;
        } else if (8 == scale) {
            //64位占8个字节
            REF_ELEMENT_SHIFT = 3;
        } else {
            throw new IllegalStateException("Unknown pointer size");
        }
        //数组对象的起始偏移量
        REF_ARRAY_BASE = UNSAFE.arrayBaseOffset(Thread[].class);
    }

    protected AbstractBatchProcessor(int batchSize, Supplier<R> select) {
        this.batchSize = batchSize;
        this.select = select;
        init();
    }

    protected AbstractBatchProcessor(Supplier<Integer> count, int batchSize) {
        this.batchSize = batchSize;
        this.count = count;
        init();
    }

    public AbstractBatchProcessor<E, R> partition(int partitionSize) {
        this.partition = (int) Math.ceil(getTotal()*1f/partitionSize);
        this.partitionSize = partitionSize/batchSize;
        this.partitionAble = true;
        return this;
    }

    public AbstractBatchProcessor<E, R> partitionLimit(int partitionLimit) {
        this.partitionLimit = partitionLimit;
        return this;
    }

    abstract protected void init();

    /**
     * 分批处理，直接循环
     * @param handler
     */
    public void forEach(Consumer<E> handler) {
        int localBatchCount = batchCount;
        int localPageNum = pageNum;
        for (int i = 0; i < localBatchCount; i++) {
            Collection<E> dataList = takeTask(localPageNum++);
            if(!CollectionUtils.isEmpty(dataList)) {
                for (E data : dataList) {
                    handler.accept(data);
                }
            }
        }
    }

    /**
     * 分批处理整批的数据
     * @param handler
     */
    public void batchExecute(Consumer<R> handler) {
        int localBatchCount = batchCount;
        int localPageNum = pageNum;
        for (int i = 0; i < localBatchCount; i++) {
            R dataList = takeTask(localPageNum++);
            if(!CollectionUtils.isEmpty(dataList)) {
                handler.accept(dataList);
            }
        }
    }

    /**
     * 并发分批执行
     * @param handler
     * @param e
     * @throws InterruptedException
     */
    public void batchExecuteConcurrently(BiConsumer<R, Integer> handler, ThreadPoolExecutor e) throws InterruptedException {
        int localBatchCount = batchCount;
        if(localBatchCount == 1) {
            runSingle(handler, 0);
            return;
        }
        int localPageNum = pageNum;
        CountDownLatch latch = new CountDownLatch(localBatchCount);
        for (int i = 0; i < localBatchCount; i++) {
            int finalIi = i;
            e.execute(() -> {
                try {
                    R r = takeTask(localPageNum + finalIi);
                    handler.accept(r, finalIi);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }

    /**
     * 并发分批处理，对结果集顺序执行
     * @param handler
     * @param e
     * @throws InterruptedException
     */
    public void batchExecuteOrderly(BiConsumer<R, Integer> handler, ThreadPoolExecutor e) throws InterruptedException {
        int localBatchCount = batchCount;
        int localPageNum = pageNum;
        if(localBatchCount == 1) {
            runSingle(handler, 0);
            return;
        }
        Thread[] stack = new Thread[NumberUtil.sizeFor(e.getMaximumPoolSize())];
        indexMask = stack.length - 1;
        CountDownLatch latch = new CountDownLatch(localBatchCount);
        for (int i = 0; i < localBatchCount; i++) {
            int index = i;
            e.execute(() -> {
                boolean isOutCountDown = false;
                try {
                    if(cancel.get()) {
                        isOutCountDown = true;
                        return;
                    }
                    R r = takeTask(localPageNum + index);
                    if(useCAS) {
                        doHandleCASOrderly(stack, latch, index, handler, r);
                    } else {
                        doHandleOrderly(stack, latch, index, handler, r);
                    }
                } catch (InterruptedException interruptedException){
                    logger.error("上一任务执行失败，本任务中断结束", interruptedException);
                    isOutCountDown = true;
                } catch (Exception exception) {
                    //非BatchProcessorException异常需要外部countDown
                    isOutCountDown = !(exception instanceof BatchHandleException);
                    logger.error("任务执行失败", isOutCountDown?exception:exception.getCause());
                    //取消任务，并终止其他线程
                    doCancel(stack, index, exception);
                    throw exception;
                } finally {
                    if(isOutCountDown) {
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
    }

    /**
     * 分片并发处理，对每片各自的结果集顺序执行
     * @param handler
     * @param e
     * @throws InterruptedException
     */
    public void batchPartitionExecuteOrderly(BiConsumer<R, Integer> handler, ThreadPoolExecutor e) throws InterruptedException {
        int localBatchCount = batchCount;
        int localPageNum = pageNum;
        int partitionSize0 = partitionSize;
        int partition0 = partition;
        int partitionLimit0 = partitionLimit;
        if(localBatchCount == 1) {
            runSingle(handler, 0);
            return;
        }
        if(!partitionAble) {
            batchExecuteOrderly(handler, e);
            return;
        }
        AtomicInteger[] swaps = new AtomicInteger[partition0];
        for (int i = 0; i < partition0; i++) {
            if(i < partition0 - 1) {
                swaps[i] = new AtomicInteger(partitionSize0);
            } else {
                swaps[i] = new AtomicInteger(localBatchCount - partitionSize0 * (partition0 - 1));
            }
        }
        CountDownLatch latch = new CountDownLatch(localBatchCount);
        int size = NumberUtil.sizeFor(e.getMaximumPoolSize());
        Thread[][] stacks = new Thread[partition0][size];
        indexMask = size - 1;
        int[] posArr = new int[partition0];
        for (int i = 0; i < posArr.length; i++) {
            posArr[i] = -1;
        }
        PartitionUtil.partition(localBatchCount, partitionSize0, partitionLimit0 != -1 && partition0>partitionLimit0?partitionLimit0*partitionSize0:partition0*partitionSize0, node -> {
            e.execute(() -> {
                boolean isOutCountDown = false;
                try {
                    if(cancel.get()) {
                        isOutCountDown = true;
                        return;
                    }
                    int realIndex = node.index;
                    R r = takeTask(localPageNum + realIndex);
                    doHandleCASOrderly(swaps, stacks, latch, realIndex, posArr, handler, r);
                } catch (InterruptedException interruptedException){
                    logger.error("上一任务执行失败，本任务中断结束", interruptedException);
                    isOutCountDown = true;
                } catch (Exception exception) {
                    //非BatchProcessorException异常需要外部countDown
                    isOutCountDown = !(exception instanceof BatchHandleException);
                    logger.error("任务执行失败", isOutCountDown?exception:exception.getCause());
                    //取消任务，并终止其他线程
                    doCancel(stacks[node.index/partitionSize0], node.index % partitionSize0, exception);
                    throw exception;
                } finally {
                    if(isOutCountDown) {
                        latch.countDown();
                    }
                }
            });
        });
        latch.await();
    }

    private void doHandleCASOrderly(AtomicInteger[] swaps, Thread[][] stack0, CountDownLatch latch, int index, int[] posArr, BiConsumer<R, Integer> handler, R r) throws InterruptedException {
        int partitionSize0 = partitionSize;
        int offsetIndex = index % partitionSize0;
        int posIndex = index / partitionSize0;
        int partition0 = partition;
        Thread[] stack = stack0[posIndex];
        AtomicInteger swap = swaps[posIndex];
        if(cancel.get()) {
            setAt(stack, offsetIndex, null);
            throw new InterruptedException("task is cancel");
        }
        int next = offsetIndex + 1;
        int count = index/partitionSize0==partition0-1?batchCount-partitionSize0*(partition0-1):partitionSize0;
        if(count - offsetIndex == swap.get()) {
            Thread nextThread;
            try {
                handler.accept(r, index);
            } catch (Exception e) {
                //终止其他线程
                doCancel(stack, offsetIndex, e);
                throw new BatchHandleException(e);
            } finally {
                swap.compareAndSet(count - offsetIndex, count - next);
                latch.countDown();
                NumberUtil.setAt(posArr, posIndex, offsetIndex);
                if(count > next && (nextThread = getAt(stack, next)) != null) {
                    LockSupport.unpark(nextThread);
                }
            }
        } else {
            Thread currentThd = Thread.currentThread();
            setAt(stack, offsetIndex, currentThd);
            if(cancel.get()) {
                setAt(stack, offsetIndex, null);
                throw new InterruptedException("task is cancel");
            }
            if(NumberUtil.getAt(posArr, posIndex) + 1 != offsetIndex) {
                LockSupport.park();
            }
            //前面线程如果执行报错，这里需要中断该线程
            if(currentThd.isInterrupted()) {
                throw new InterruptedException("task is cancel");
            }
            setAt(stack, offsetIndex, null);
            doHandleCASOrderly(swaps, stack0, latch, index, posArr, handler, r);
        }
    }

    /**
     * 执行单个任务
     * @param handler
     * @param index
     */
    private void runSingle(BiConsumer<R, Integer> handler, int index) {
        try {
            R r = select.get();
            if(!CollectionUtils.isEmpty(r)) {
                handler.accept(r, index);
            }
        } catch (Exception exception) {
            if(!CollectionUtils.isEmpty(exceptionHandlers)) {
                for (Consumer<Exception> exceptionHandler : exceptionHandlers) {
                    exceptionHandler.accept(exception);
                }
            }
            throw exception;
        }
    }

    abstract protected R takeTask(int index);

    /**
     * 支持重试的查询
     * @param tryTimes
     * @return
     */
    protected R reryAbleSelect(int tryTimes) {
        try {
            return select.get();
        } catch (Exception e) {
            if(tryTimes < maxRetryTimes) {
                return reryAbleSelect(++tryTimes);
            }
            throw e;
        }
    }

    /**
     * 对结果顺序执行
     * @param latch
     * @param index
     * @param handler
     * @param r
     * @throws InterruptedException
     */
    private void doHandleOrderly(Thread[] stack, CountDownLatch latch, int index, BiConsumer<R, Integer> handler, R r) throws InterruptedException {
        synchronized (latch) {
            if(cancel.get()) {
                stack[index] = null;
                throw new InterruptedException("task is cancel");
            }
            int count = batchCount;
            if (count - index == latch.getCount()) {
                try {
                    handler.accept(r, index);
                    if(count > index + 1 && stack[index + 1] != null) {
                        latch.notifyAll();
                    }
                } catch (Exception e){
                    //终止其他线程
                    doCancel(stack, index, e);
                    throw new BatchHandleException(e);
                } finally {
                    latch.countDown();
                }
            } else {
                stack[index] = Thread.currentThread();
                if(cancel.get()) {
                    stack[index] = null;
                    throw new InterruptedException("task is cancel");
                }
                try {
                    latch.wait();
                } catch (InterruptedException interruptedException) {
                    stack[index] = null;
                    throw interruptedException;
                }
                doHandleOrderly(stack, latch, index, handler, r);
            }
        }
    }

    /**
     * 通过CAS保证对结果的顺序处理
     * @param latch
     * @param index
     * @param handler
     * @param r
     * @throws InterruptedException
     */
    private void doHandleCASOrderly(Thread[] stack, CountDownLatch latch, int index, BiConsumer<R, Integer> handler, R r) throws InterruptedException {
        if(cancel.get()) {
            setAt(stack, index, null);
            throw new InterruptedException("task is cancel");
        }
        int next = index + 1;
        int count = batchCount;
        if(count - index == latch.getCount()) {
            Thread nextThread;
            try {
                handler.accept(r, index);
            } catch (Exception e) {
                //终止其他线程
                doCancel(stack, index, e);
                throw new BatchHandleException(e);
            } finally {
                latch.countDown();
                pos = index;
                if(count > next && (nextThread = getAt(stack, next)) != null) {
                    LockSupport.unpark(nextThread);
                }
            }
        } else {
            Thread currentThd = Thread.currentThread();
            setAt(stack, index, currentThd);
            if(cancel.get()) {
                setAt(stack, index, null);
                throw new InterruptedException();
            }
            if(pos + 1 != index) {
                LockSupport.park();
            }
            //前面线程如果执行报错，这里需要中断该线程
            if(currentThd.isInterrupted()) {
                throw new InterruptedException();
            }
            setAt(stack, index, null);
            doHandleCASOrderly(stack, latch, index, handler, r);
        }
    }

    /**
     * 对数组元素的可见性set
     * @param index
     * @param thread
     */
    private void setAt(Thread[] stack, int index, Thread thread) {
        UNSAFE.putObjectVolatile(stack, REF_ARRAY_BASE + ((index & indexMask) << REF_ELEMENT_SHIFT), thread);
    }

    /**
     * 对数组元素的可见性get
     * @param index
     * @return
     */
    private Thread getAt(Thread[] stack, int index) {
        return (Thread) UNSAFE.getObjectVolatile(stack, REF_ARRAY_BASE + ((index & indexMask) << REF_ELEMENT_SHIFT));
    }

    /**
     * CAS set
     * @param index
     * @param c
     * @param v
     * @return
     */
    private boolean casAt(Thread[] stack, int index, Thread c, Thread v) {
        return UNSAFE.compareAndSwapObject(stack, REF_ARRAY_BASE + ((index & indexMask) << REF_ELEMENT_SHIFT), c, v);
    }

    /**
     * 取消任务，并终止其他线程
     * @param startIndex
     */
    private void doCancel(Thread[] stack, int startIndex, Exception e) {
        int realStartIndex = Math.min(startIndex, pos);
        //取消任务
        if(cancel.compareAndSet(false, true) && !CollectionUtils.isEmpty(exceptionHandlers)) {
            for (Consumer<Exception> exceptionHandler : exceptionHandlers) {
                exceptionHandler.accept(e);
            }
        }
        //终止其他线程
        for (int j = realStartIndex + 1; j < realStartIndex + stack.length; j++) {
            Thread thread = getAt(stack, j);
            if(thread != null && casAt(stack, j, thread, null)) {
                thread.interrupt();
            }
        }
    }

    public AbstractBatchProcessor<E, R> addExceptionHandler(Consumer<Exception> exceptionHandler) {
        this.exceptionHandlers.add(exceptionHandler);
        return this;
    }

    public int getTotal() {
        return total;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public AbstractBatchProcessor<E, R> maxRetryTime(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
        return this;
    }

    public void execute(ThreadPoolExecutor e) throws InterruptedException {
        throw new RuntimeException("该方法需要子类实现");
    }
}


