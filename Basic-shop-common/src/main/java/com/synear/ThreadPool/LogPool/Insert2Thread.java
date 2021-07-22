package com.synear.ThreadPool.LogPool;

import com.synear.ThreadPool.pojo.LogBean;
import com.synear.ThreadPool.service.LogService;
import org.apache.log4j.Logger;


public class Insert2Thread implements Runnable {

    private final Logger logger = Logger.getLogger(this.getClass());

    private LogService testLogService;

    private LogBean logBean;

    public Insert2Thread(LogService testLogService, LogBean logBean) {
        this.testLogService = testLogService;
        this.logBean = logBean;
    }

    /**
     * 日志入库操作
     */
    @Override
    public void run() {
        logger.info(Thread.currentThread().getName()+"====> 进行日志记录入库...");
        testLogService.batchInsert2(logBean);
        // help GC
        logBean = null;
    }
}
