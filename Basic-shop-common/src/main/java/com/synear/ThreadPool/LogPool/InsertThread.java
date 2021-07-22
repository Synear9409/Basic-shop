package com.synear.ThreadPool.LogPool;

import com.synear.ThreadPool.pojo.LogBean;
import com.synear.ThreadPool.service.LogService;
import org.apache.log4j.Logger;

import java.util.List;


public class InsertThread implements Runnable {

    private final Logger logger = Logger.getLogger(this.getClass());

    private LogService testLogService;

    private List<LogBean> list;

      public InsertThread(LogService testLogService, List<LogBean> list) {
        this.testLogService = testLogService;
        this.list = list;
    }


    /**
     * 日志入库操作
     */
    @Override
    public void run() {
        logger.info(Thread.currentThread().getName()+"====> 进行日志记录入库...");
        testLogService.batchInsert(list);
        // help GC
        list = null;
    }
}
