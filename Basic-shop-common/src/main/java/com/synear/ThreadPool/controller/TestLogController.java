package com.synear.ThreadPool.controller;

import com.synear.ThreadPool.LogPool.Insert2Thread;
import com.synear.ThreadPool.LogPool.LogPoolManager;
import com.synear.ThreadPool.LogPool.LogPoolManager2;
import com.synear.ThreadPool.pojo.LogBean;
import com.synear.ThreadPool.service.LogService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/v1/api")
public class TestLogController {

    private final org.apache.log4j.Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private LogPoolManager logPoolManager;

    private LogPoolManager2 logPoolManager2 = LogPoolManager2.newInstance();

    @Autowired
    private LogService logService;


    @GetMapping("/log/test")
    public void logTest() {
        // 此处可以写具体的业务逻辑

        LogBean testLogBean = new LogBean();
        testLogBean.setLogContent("Test log , Test log " + UUID.randomUUID().toString());
        // 将业务日志放入到队列中，然后使用线程 异步 批量进行入库，以提升接口的响应速度
        try {
            logPoolManager.addLog(testLogBean);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("log offer queue success !");
    }

    @GetMapping("/log/test2")
    public void logTest2() {
        // 此处可以写具体的业务逻辑

        LogBean testLogBean = new LogBean();
        testLogBean.setLogContent("Test log , Test log " + UUID.randomUUID().toString());
        try {
            logPoolManager2.addExecuteTask(new Insert2Thread(logService, testLogBean));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("log task run success !");
    }

}
