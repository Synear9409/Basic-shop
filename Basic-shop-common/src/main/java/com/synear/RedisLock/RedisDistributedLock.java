package com.synear.RedisLock;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;

/**
 * redis 分布式锁实现
 */
public class RedisDistributedLock {

    public static Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    // 加锁成功标志
    private static final String LOCK_SUCCESS = "OK";

    // 释放锁成功标志
    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * redis
     */
    private Jedis jedis;

    /**
     * 分布式锁键值
     */
    private String lockKey;

    /**
     * 锁的超时时间
     */
    private int expireTime = 10 * 1000;

    /**
     * 锁等待，防止线程饥渴
     */
    private int acquireTimeout = 1000;

    /**
     * 获取指定键值的锁
     * @param jedis redis客户端
     * @param lockKey 锁的键值
     */
    public RedisDistributedLock(Jedis jedis, String lockKey) {
        this.jedis = jedis;
        this.lockKey = lockKey;
    }

    /**
     * 获取指定键值的锁,同时设置获取锁超时时间
     * @param jedis redis客户端
     * @param lockKey 锁的键值
     * @param acquireTimeout 获取锁超时时间
     */
    public RedisDistributedLock(Jedis jedis, String lockKey, int acquireTimeout) {
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.acquireTimeout = acquireTimeout;
    }

    /**
     * 获取指定键值的锁,同时设置获取锁超时时间和锁过期时间
     * @param jedis redis客户端
     * @param lockKey 锁的键值
     * @param acquireTimeout 获取锁超时时间
     * @param expireTime 锁失效时间
     */
    public RedisDistributedLock(Jedis jedis, String lockKey, int acquireTimeout, int expireTime) {
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.acquireTimeout = acquireTimeout;
        this.expireTime = expireTime;
    }

    /**
     * 获取锁
     * @return
     */
    public String acquire() {

        try {
            long end = System.currentTimeMillis() + acquireTimeout;

            // 生成一个客户端标志（分布式中很多进程[客户端]，用来辨识是哪个客户端在操作锁）
            String token = UUID.randomUUID().toString();

            while (System.currentTimeMillis() < end) {
                SetParams params = new SetParams();
                params.nx();  // Only set the key if it does not already exist
                params.px(expireTime);  // 设置键key的过期时间，单位时毫秒
                String result = jedis.set(lockKey, token, params);
                // 对应redis命令： SET lockKey token NX PX expireTime
                if (LOCK_SUCCESS.equals(result)) {  // 获取锁成功，返回客户端标识
                    System.out.println(Thread.currentThread().getName()+"拿到了锁！");
                    return token;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();   // 出现异常则中断该线程
                }
            }
        }catch (Exception e) {
            logger.error("acquire lock due to error", e);
        }
        return null;

    }

    /**
     * 释放锁
     * @param identify
     * @return
     */
    public boolean releaseLock(String identify) {
        if (StringUtils.isBlank(identify)) {
            return false;
        }
        Object result = new Object();
        String lru_script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        try {
            result = jedis.eval(lru_script, Collections.singletonList(lockKey), Collections.singletonList(identify));
            if (RELEASE_SUCCESS.equals(result)) {   // 释放锁成功
                System.out.println(Thread.currentThread().getName()+"释放了锁！");
                logger.info("release lock success, token:{}", identify);
                return true;
            }
        }catch (Exception e) {
            logger.error("release lock due to error",e);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        logger.info("release lock failed, token:{}  , result:{}", identify, result);
        return false;

    }

}
