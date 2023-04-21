# concurrent-excel
流式并发excel导出，支持亿级数据量导出

【功能】注解式列名、格式转换  
【功能】高效率导出，异步任务自动分片，支持多线程分片处理和导出，并能保证导出的数据的有序性  
【功能】治理超大批量导出的内存和磁盘飙满的风险  
【功能】支持http直接导出、OSS上传、可查询下载进度等方式

###http直接下载导出
####自动分页导出
```java
private static void export1() throws InterruptedException {
        //一次批量查2000条，每个文件限制50000条，一个文件只支持一个sheet页
        ExcelUtil.buildHttpDownLoadHandler(() -> {
            //查库，分页插件只对方法的第一条sql生效
            return null;
        }, 2000, 50000)
        //开启分片下载，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
        .partition(true)
        //并发分片的限制，即同时写的文件数量限制在设定的值，开启分片后默认20
        .partitionLimit(10)
        .addExceptionHandler(exception -> {
            //异步异常处理，并发任务只会进入一次异常处理
        })
        //传入pool线程池
        .execute(null, pool);
}
```
####手动分页导出
```java
private static void export3() throws InterruptedException {
        ExcelUtil.buildSimpleHttpDownLoadBuilder(() -> {
            //查总数方法
            return count;
        }, 2000, 50000)
        .customSelect(batchParam -> {
            //分页查询（batchIndex：页数，batchSize：每页数量，batchStart：当前页开始的条数）
            return new ArrayList<>();
        })
        //开启分片下载，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
        .partition(true)
        //并发分片的限制，即同时写的文件数量限制在设定的值，开启分片后默认20
        .partitionLimit(10)
        .build()
        .execute(null, ThreadPool.pool);
    }
```
###超大文件上传OSS
```java
private static void customPageExport() throws InterruptedException {
        ProgressbarContext context = new ProgressbarContext();
        int count = 1000001;
        long start = System.currentTimeMillis();
        new Thread(() -> {
            try {
                ExcelUtil.buildSimpleOssDownLoadProcessorBuilder(() -> count
                                , 2000, 50000, null, context)
                        .ossAction(inputStream -> {
                            //通过inputStream上传OSS操作
                            System.out.println("开始上传OSS：inputStream" + inputStream);
                            try {
                                //模拟OSS上传
                                FileUtils.uploadByPipedStream(inputStream, context.getTableName() + ".zip");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("导出完成");
                        })
                        //进度条粒度，50代表每完成2%刷新一次进度，默认20，即每完成5%刷新一次进度
                        .processPart(50)
                        //自定义分页获取数据的方法
                        .customSelect(batchParam -> DataFactory.get(batchParam, count))
                        //分片并发任务方式，一片即一个文件，并发任务会片与片之间隔离分配，实现并发无锁方式同时写入多个excel，且能保证写入数据有序性
                        .partition(true)
                        //并发分片的限制，即同时写的文件数量限制在设定的值，开启分片后默认20
                        .partitionLimit(20)
                        .build()
                        .addExceptionHandler(exception -> {
                            //异步异常处理
                        })
                        .execute(null, ThreadPool.pool);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();;
        while(context.getProgress() != 100) {
            System.out.println("当前进度：" + context.getProgress());
            Thread.sleep(1000L);
        }
        System.out.println(context.getProgress());
        System.out.println(System.currentTimeMillis() - start);
    }
```


