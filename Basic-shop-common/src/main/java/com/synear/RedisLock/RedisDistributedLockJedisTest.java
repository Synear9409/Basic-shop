package com.synear.RedisLock;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDistributedLockJedisTest {

    public static int n = 500;
    public static void decSum() {
        System.out.println(--n);
    }

    public static void main(String[] args) {
        Runnable runnable = () -> {
            RedisDistributedLock distributedLock = null;
            Jedis jedis = null;
            String token = null;  // 客户端标识  加锁成功则可返回此token
            try {
                jedis = new Jedis("192.168.137.100", 6379);
                jedis.auth("123456");
                distributedLock = new RedisDistributedLock(jedis, "Test");

                /**
                 * while循环分析
                 * 这里十个线程一起去跑，肯定有某一个先拿到，没拿到的那些线程先sleep一下
                 * 接着第一个拿到的线程已经释放锁，其他线程继续尝试拿
                 */
                while (StringUtils.isBlank(token)) {
                    token = distributedLock.acquire();
                }

//                System.out.println(Thread.currentThread().getName() + "正在运行");
                decSum();
            } finally {
                if (distributedLock != null) {
                    distributedLock.releaseLock(token);
                }
            }
        };

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}
