# concurrent-excel
并发excel导出

支持亿级导出，且能保证导出行的顺序（需保证线程池队列FIFO），需要合理配置线程池，如最大线程数为5，分批查询一次查2000条，那么内存中同时存在最大条数为1万
