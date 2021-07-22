package com.synear.ThreadPool.Demo;

import java.util.concurrent.*;

public class CallableAndRunnableTest {

    public static void main(String[] args) throws Exception {
        System.out.println(" =========> main start ");
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        Future<String> submit = threadPoolExecutor.submit(new MyCallable());  // 注意异常会被隐藏掉，若需获取异常则调用FutureTask.get()方法获取
        try {
            TimeUnit.SECONDS.sleep(2);
//            System.out.println(submit.get());  // 这是一种做法  第二种做法看LogPoolManager2类里的namedThreadFactory属性
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(" =========> main end ");
    }

}
