package com.synear.DesignMode.Singleton;


/**
 * 懒汉式---双端检索
 */
public class Singleton {

    public static volatile Singleton instance = null;

    public Singleton(){};

    public Singleton getInstance() {
        // 先检查实例是否存在，不存在则进入同步块
        if (instance == null) {

            synchronized (Singleton.class) {
                // 再次检查实例是否存在，不存在则实例化
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }

}
