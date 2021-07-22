package com.synear.ThreadPool.controller;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Test {

    //重入锁
    private final Lock lock = new ReentrantLock();
    private int count;
    public void incr() {
        // 访问count时，需要加锁
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    public int getCount() {
        //读取数据也需要加锁，才能保证数据的可见性
        lock.lock();
        try {
            return count;
        }finally {
            lock.unlock();
        }
    }
    public static void main(String[] args) throws InterruptedException {


        Test test = new Test();
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                test.incr();
            }).start();
        }

        System.out.println("count:"+test.getCount());
    }
}
