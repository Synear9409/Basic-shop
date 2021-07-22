package com.synear.ThreadPool.Demo;

import java.util.concurrent.Callable;

public class MyCallable implements Callable {
    @Override
    public Object call() throws Exception {
        // 第三种做法 主动在call里try catch
        try {
            System.out.println("===> 开始执行callable");
            int i = 1 / 0; //异常的地方
        }catch (Exception e){
            //异常处理
            System.out.println("===> 糟啦 ， callable 执行异常： " + e);
        }
        return "callable的结果";
    }
}
