package com.synear.ThreadPool.listener;

import com.synear.ThreadPool.LogPool.LogPoolManager;
import com.synear.ThreadPool.LogPool.LogPoolManager2;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


/**
 * 项目启动监听器，启动时进行数据初始化、以及启动各模块组件
 */
@WebListener
@Component
public class InitServletContextListener implements ServletContextListener {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private LogPoolManager logPoolManager;

    private LogPoolManager2 logPoolManager2;


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 日志异步池化处理 启动
//        logPoolManager.init();
        logPoolManager2 = LogPoolManager2.newInstance();
        logger.info("日志异步池化处理启动成功.....");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 关闭线程池
        logPoolManager2.shutdown();
//        logPoolManager.shutDown();
    }
}
