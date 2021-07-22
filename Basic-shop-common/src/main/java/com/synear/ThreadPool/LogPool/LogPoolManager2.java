package com.synear.ThreadPool.LogPool;



import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * 第二种思路写法
 * 日志线程池管理(线程统一调度管理)  // 【采用到单例模式--懒汉式 线程安全】
 */

public class LogPoolManager2 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static LogPoolManager2 logPoolManager = new LogPoolManager2();

    // 线程池核心线程的数量
    private static final int SIZE_CORE_POOL = 5;

    // 线程池维护线程的最大数量
    private static final int SIZE_MAX_POOL = 10;

    // 线程池维护线程所允许的空闲时间
    private static final long TIME_KEEP_ALIVE = 5000;

    // 线程池所使用的缓冲队列大小
    private static final int SIZE_WORK_QUEUE = 100;

    // 任务调度周期
    private static final int PERIOD_TASK_QOS = 1000;

    public static LogPoolManager2 newInstance() {
        return logPoolManager;
    }

    /*
     * 将构造方法访问修饰符设为私有，禁止任意实例化。
     */
    private LogPoolManager2() {
        mTaskHandler = scheduler.scheduleAtFixedRate(mAccessBufferThread, 0,
                PERIOD_TASK_QOS, TimeUnit.MILLISECONDS);
    }

    // 任务缓存队列
    private final Queue<Runnable> taskQueue = new LinkedList<>();

    //自定义线程工厂 -- 为每个线程附上自定义的名字
    private final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("synear-pool-%d")
            // 当使用mThreadPool.submit配合Callable使用时，如果内部线程发生异常，需要主动调用FutureTask.get()方法获取异常,不然就用这种构建异常捕获器
            .setUncaughtExceptionHandler((thread, throwable) -> {logger.error("ThreadPool {} got exception", thread,throwable);})
            .build();

    /*
     * 创建一个调度线程池
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /*
     * 通过调度线程周期性的执行缓冲队列中任务
     */
    protected final ScheduledFuture<?> mTaskHandler;

    /*
     * 自定义饱和策略
     * 线程池超出界线时将任务加入缓冲队列
     */
    private final RejectedExecutionHandler mHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            taskQueue.offer(r);
        }
    };

    /*
     * 线程池
     * author: Synear
     *
     * 参数1：corePoolSize 核心线程数
     * 1、当前线程数未等于corePoolSize时，则继续创建线程执行任务，
     * [即使前面的线程可能空闲了，但新任务来还是会创建新的线程执行]
     * 2、当线程数大于等于corePoolSize时，继续提交的任务会被保存进缓存队列中，等待被执行
     *
     *
     * 参数5：缓存队列 -- 用于在执行任务之前保存任务的队列。这个队列将只保存execute方法提交的
     * 1、ArrayBlockingQueue 基于数组结构的有界阻塞队列，按FIFO排序任务；
     * 2、LinkedBlockingQueue：基于链表结构的阻塞队列，按FIFO排序任务，吞吐量通常要高于ArrayBlockingQueue；
     * 3、SynchronousQueue：一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQueue；
     * 4、priorityBlockingQueue：具有优先级的无界阻塞队列；
     *
     * 对比：LinkedBlockingQueue比ArrayBlockingQueue在插入删除节点性能方面更优，
     * 但是二者在put(), take()任务的时均需要加锁，SynchronousQueue使用无锁算法，
     * 根据节点的状态判断执行，而不需要用到锁，其核心是Transfer.transfer().
     *
     * 自定义线程工厂，用于为线程池中的每条线程命名
     * ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("stats-pool-%d").build();
     *
     *
     *
     * 参数6：handler 饱和策略
     * 默认的饱和策略如下：  【默认的策略只有第二个可以保证数据的完整性，如果要求数据完整性还是自定义饱和策略】
     * 1、AbortPolicy：直接抛出异常，默认策略；
     * 2、CallerRunsPolicy：用调用者所在的线程来执行任务；
     * 3、DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务；
     * 4、DiscardPolicy：直接丢弃任务；
     *
     * 直接通过Executors 创建几种常见线程池的缺点：
     * newFixedThreadPool [该线程池重用固定数量 nThread 的线程]
     * 1、该线程池的线程数量达corePoolSize后，即使线程池没有可执行任务时，也不会释放线程
     * 2、FixedThreadPool的工作队列为无界队列LinkedBlockingQueue(队列容量为Integer.MAX_VALUE)
     *  2.1、线程池里的线程数量不超过corePoolSize,这导致了maximumPoolSize和keepAliveTime将会是个无用参数
     *  2.2、由于使用了无界队列, 所以FixedThreadPool永远不会拒绝, 即饱和策略失效
     *  2.3、若没控制好程序，可能会导致服务器cpu长时间飙高
     *
     * newSingleThreadExecutor [该线程池只存在一个线程]
     * 1、若该线程出现异常，会重新创建新的一个线程执行，从始至终保持一个线程的存在
     * 2、和上面的线程一样，工作队列也是无界队列 缺点如上
     *
     * newCachedThreadPool (比较适用于短期要进行数据入库或者什么的，对于断断续续的任务它存在一个线程切换的消耗的问题)
     * 1、线程池的最大线程数可达到Integer.MAX_VALUE，即2147483647，内部使用SynchronousQueue作为阻塞队列
     * 2、与newFixedThreadPool线程池不同，newCachedThreadPool在没有任务执行时,等待时间超过
     * keepAliveTime时，会自动释放线程资源，当提交新任务时，如果没有空闲线程，则创建新线程执行任务，
     * 会导致一定的系统开销；
     * 而newFixedThreadPool是除非你主动去shutdown，否则其创建的固定数量核心线程会一直存在；
     *
     * 该线程池执行过程和前两种稍微不同：
     * 1、主线程调用SynchronousQueue的offer()方法放入task, 倘若此时线程池中有空闲的线程尝试读取 SynchronousQueue的task,
     * 即调用了SynchronousQueue的poll(), 那么主线程将该task交给空闲线程. 否则执行2
     * 2、当线程池为空或者没有空闲的线程, 则创建新的线程执行任务.
     * 3、执行完任务的线程倘若在60s内仍空闲, 则会被终止. 因此长时间空闲的CachedThreadPool不会持有任何线程资源.
     *
     *
     * 关闭线程池 原理：遍历线程池中的所有线程，然后逐个调用线程的interrupt方法来中断线程
     * 1、shutdown 将线程池里的线程状态设置成SHUTDOWN状态, 然后中断所有没有正在执行任务的线程
     * 2、shutdownNow 将线程池里的线程状态设置成STOP状态, 然后停止所有正在执行或暂停任务的线程 并将任务队列的任务清除
     *
     *
     *
     */
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL,
            TIME_KEEP_ALIVE, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(SIZE_WORK_QUEUE),namedThreadFactory,mHandler);

    /*
     * 将缓冲队列中的任务重新加载到线程池
     */
        private final Runnable mAccessBufferThread = new Runnable() {
        @Override
        public void run() {
            if (hasMoreAcquire()) {
                mThreadPool.execute(taskQueue.poll());
            }
        }
    };

    /*
     * 消息队列检查方法
     */
    private boolean hasMoreAcquire() {
        return !taskQueue.isEmpty();
    }

    /*
     * 任务执行的步骤
     * execute –> addWorker –> runWorker （getTask）
     *
     * execute()方法解析  拒绝任务的情况有两种[1、线程池状态为非RUNNING状态 2、等待队列已满。]
     * 1、通过workerCountOf获取当前线程数，小于corePoolSize，则执行addWorker创建线程执行command任务
     * 2、判断当前线程状态 [if (isRunning(c) && workQueue.offer(command))] 若线程池处于RUNNING状态，把提交的任务成功放入阻塞队列中
     * 3、double check [ if (! isRunning(recheck) && remove(command))] 再次判断线程池状态，若不是running，则在阻塞队列中删除任务，执行reject方法(就拒绝策略)处理command任务
     * 4、[if (workerCountOf(recheck) == 0)]然后线程池是running的状态但没有线程，则创建新线程
     * 5、最后一种情况则判断在线程池创建新线程是否成功，失败则reject任务[if (!addWorker(command, false))]
     *
     * 这里说下为什么需要double check这个操作？
     * 1、首先介绍线程池的状态[running ---> shutdown ----> stop ----> tidying ----> terminated]
     * 2、主要是在多线程的环境下，线程池的状态是有可能时刻在变化的，所以你无法保证你上一秒获取到的ctl.get() 这个值，在下一秒获取是还是原值，
     * 即使它是个原子类，get到值在多线程之间是立即可见的，但对于我们编码的人是无法感知它是否变化了，所以需要
     * 再次int recheck = ctl.get() recheck一次，判断线程池状态是否符合你接下来进行编码逻辑；
     * 举个例子：若此时你没有进行recheck，此时线程池的状态突然为非running，那么command任务永远不会执行，
     *
     *  addWorker()方法解析  --- 创建工作线程
     * 1、if (rs >= SHUTDOWN && ! (rs == SHUTDOWN &&firstTask == null &&! workQueue.isEmpty()))
     *  首先判断线程池状态值rs是否大于等于shutdown 并且 rs不等于SHUTDOWN 或 firstTask != null 或 工作队列为空时 直接返回false
     * 2、其次会判断当前的工作线程数，是否大于核心线程数 或 最大线程数 取决与传入的core参数，若大于则直接返回false
     * 3、采用cas对当前工作线程数+1，修改成功则直接跳出最外层循环，进行worker对象创建
     * 4、再次获取ctl.get()的值，原因与execute的double check作用类似
     * 然后将得到新的线程池状态值runStateOf(c)，如果与外层for循环的rs值不等，则continue此次内循环，
     * 否则则说明cas操作失败，重试内循环
     * 5、创建worker对象，判断worker对象的thread属性不为空时，然后加锁，获得线程池状态值rs
     * 6、if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
     * 若小于SHUTDOWN则为Running状态，或者当rs等于SHUTDOWN并且传入的firstTask为空，则判断thread是否isAlive
     *  若是则抛出IllegalThreadStateException异常，否则将创建的worker对象加入HashSet<worker>中；
     *7、获取hashSet的size,若size大于largestPoolSize(初始化默认值为0)，则直接对largestPoolSize赋值为size
     * 并修改workerAdded标志为true，释放锁，判断workerAdded，true则开启thread线程，并workerStarted赋值为true
     * 最后判断workerStarted为false，则调用addWorkerFailed添加工作线程失败
     *
     * runWorker()方法解析 --- 线程池的核心 执行任务
     * 1. 线程启动之后，通过unlock方法释放锁，设置AQS的state为0，表示运行可中断；
     * 2. Worker执行firstTask或从workQueue中获取任务：
     * 2.1. 进行加锁操作，保证thread不被其他线程中断（除非线程池被中断）
     * 2.2. 检查线程池状态，倘若线程池处于中断状态，当前线程将中断。
     * 2.3. 执行beforeExecute
     * 2.4 执行任务的run方法
     * 2.5 执行afterExecute方法
     * 2.6 解锁操作
     *
     *
     *
     *
     */
    // 向线程池中添加任务方法
    public void addExecuteTask(Runnable task) {
        if (task != null) {
            mThreadPool.execute(task);
        }
    }

    public void shutdown() {
        logger.info("LogPoolManager2 Thread Pool shutdown...");
        taskQueue.clear();
        mThreadPool.shutdown();
    }



}
