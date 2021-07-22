package com.synear.CacheBreak;


import com.synear.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 缓存击穿 解决方案
 * 1、设置热点数据永不过期
 * 2、互斥锁
 */
@Component
public class CacheBreak {

    @Autowired
    private RedisUtil redisUtil;

    private static ReentrantLock reentrantLock = new ReentrantLock();

    /**
     * 互斥锁方案实现
     */
    public String getData(String key) throws InterruptedException {

        // 从redis中取数据
        String result = redisUtil.get(key);
        // 参数校验
        if (StringUtils.isBlank(result)) {   //缓存中没有

            try{
                // 获取锁
                if (reentrantLock.tryLock()) {

                    // 去数据库查
//                    result = getDataByDB(key);
                    //校验
                    if (StringUtils.isNotBlank(result)) {
                        // 存进缓存里
                        redisUtil.set(key, result);
                    }

                } else {
                    // 睡一会再拿
                    Thread.sleep(3000);
                    result = getData(key);
                }
            }finally {
                // 释放锁
                reentrantLock.unlock();
            }
        }
        return result;
    }

}
