package com.synear.ThreadPool.LogPool;

import com.synear.ThreadPool.pojo.LogBean;
import com.synear.ThreadPool.service.LogService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 日志 池化管理
 * 利用线程池，将日志批量操作加入队列中，异步进行入库
 * 优点： 提高了响应速度
 */

@Component
@Scope("singleton")
public class LogPoolManager {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private LogService logService;

    /**
     * 要保留的线程数，即使处于空闲状态
     */
    private static final int corePoolSize = 5;

    /**
     * 工作线程池核心线程数
     */
    private static final int workCorePoolSize = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 池中最大允许存在的线程数
     */
    private static final int maximumPoolSize = 10;

    /**
     * 前提：当当前线程数大于核心数时！！！！，多余空闲线程在终止前等待新任务的最长时间
     */
    private static final long keepAliveTime = 1L;

    /**
     * 创建一个工作线程池（专门用来日志入库操作）
     */
    private ExecutorService logWorkerThreadPool;

    /**
     * 日志线程池
     */
    private ThreadPoolExecutor logThreadPool;

    /**
     * 任务缓冲队列，存放当前线程处理不来的日志任务
     */
    private BlockingQueue<Runnable> workQueue;

    /**
     * 日志队列 的最大容量
     */
    private int MAX_QUEUE_SIZE = 100;

    /**
     *  日志批量插入的数量，10条日志
     */
    private int BATCH_SIZE = 10;

    /**
     * 线程睡眠时间，具体时间需要结合项目实际情况，单位毫秒
     */
    private int SLEEP_TIME = 500;

    /**
     * 日志插入执行的最大的时间间隔，单位毫秒
     */
    private long MAX_EXE_TiME = 5000;

    /**
     * Boolean 原子变量类
     */
    private AtomicBoolean run = new AtomicBoolean(true);

    /**
     * 整型 原子变量类，记录 任务队列 中的任务数量
     */
    private AtomicInteger logCount = new AtomicInteger(0);

    /**
     * 上一次执行日志插入时的时间
     */
    private long lastExecuteTime;

    /*
     * 线程池超出界线时将任务加入缓冲队列
     * 自定义饱和策略 -- 实现RejectedExecutionHandler接口
     */
    private final RejectedExecutionHandler mHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            workQueue.offer(r);
        }
    };

    /**
     *  日志线程池初始化
     */
    public void init() {
        // 基于链表的双向阻塞队列，在队列的两端都可以插入和移除元素，是线程安全的，多线程并发下效率更高
        workQueue = new LinkedBlockingDeque<>(MAX_QUEUE_SIZE);
        logThreadPool = new ThreadPoolExecutor(
                                            corePoolSize,
                                            maximumPoolSize,
                                            keepAliveTime,
                                            TimeUnit.SECONDS,
                                            workQueue,
                                            mHandler);

        logWorkerThreadPool = new ThreadPoolExecutor(
                                            workCorePoolSize, //java虚拟机可用的处理器数量 * 2
                                            workCorePoolSize,
                                            keepAliveTime,
                                            TimeUnit.SECONDS,
                                            new ArrayBlockingQueue<>(100));

        lastExecuteTime = System.currentTimeMillis();

        logger.info("LogPoolManager init successfully... At:"+lastExecuteTime);

        logThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (run.get()) {
                    try {
                        // 线程休眠，具体时间根据项目的实际情况配置
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        logger.error("log Manager Thread sleep fail ", e);
                    }
                    // 满足存放了10个日志  或  满足时间间隔已经大于设置的最大时间间隔时  执行日志插入
                    if (logCount.get() >= BATCH_SIZE || (System.currentTimeMillis() - lastExecuteTime) > MAX_EXE_TiME) {
                        if (logCount.get() > 0) {
                            logger.info(Thread.currentThread().getName()+"=====> begin drain log queue to database...");
                            List list = new ArrayList<>();
                            /**
                             *  drainTo (): 一次性从BlockingQueue获取所有可用的数据对象（还可以指定获取数据的个数），
                             *  通过该方法，可以提升获取数据效率；不需要多次分批加锁或释放锁。
                             *  将取出的数据放入指定的list集合中
                             */
                            workQueue.drainTo(list);
                            // 任务队列 中任务数量置为0
                            logCount.set(0);
                            // 工作线程池 执行日志插入工作
                            logWorkerThreadPool.execute(new InsertThread(logService, list));
                            logger.info(Thread.currentThread().getName()+"=====> end drain log queue to database...");
                        }
                        // 获取当前执行的时间
                        lastExecuteTime = System.currentTimeMillis();
                    }
                }
            }
        });

    }

    /**
     *  将日志放入到 队列中
     * @param logBean
     */
    public void addLog(LogBean logBean) throws Exception{
        if (logCount.get() >= MAX_QUEUE_SIZE) {
            // 当队列满时，让线程先睡一会
            logger.warn("rejected .. Log count exceed log queue's max size ！");
            Thread.sleep(5000);
        }
        // 否则加入任务队列中, 放入成功返回true
        this.workQueue.offer(logBean);
        logger.info("日志入队成功！");
        // 队列中的任务数量 +1
        logCount.incrementAndGet();
    }

    /**
     *  关闭 线程池
     */
    public void shutDown() {
        logger.info("LogPoolManager Thread Pool shutdown...");
        // 结束while循环
        run.set(false);
        // 关闭线程池
        logWorkerThreadPool.shutdownNow();
        logThreadPool.shutdownNow();
    }



}
