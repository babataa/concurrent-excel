package com.babata;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zqj
 */
public class ThreadPool {

    public static ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10, 5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100000));
}
