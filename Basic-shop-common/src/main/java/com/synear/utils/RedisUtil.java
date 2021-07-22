package com.synear.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Synear
 * @Description: Redis操作工具类
 */

@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 实现命令： TTL key，以秒为单位，返回key的剩余时间
     * @param key
     * @return
     */
    public Long ttl(String key){
        return redisTemplate.getExpire(key);
    }

    public void expire(String key, Long timeout){
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    public Long incr(String key, Long delta){
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Set<String> keys(String pattern){
        return redisTemplate.keys(pattern);
    }

    public void del(String key){
        redisTemplate.delete(key);
    }

    public void set(String key, String value){
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout){
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }


    public String get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /* 哈希表 */

    public void hset(String key, String field, Object value){
        redisTemplate.opsForHash().put(key, field, value);
    }


    public String hget(String key, String field){
        return (String) redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 实现命令： HDEL key field [field ...], 删除哈希表key中的一个或多个指定字段
     * @param key
     * @param fields
     */
    public void hdel(String key, Object... fields){
        redisTemplate.opsForHash().delete(key, fields);
    }


    public Map<Object, Object> hgetall(String key){
        return redisTemplate.opsForHash().entries(key);
    }

    /* List */

    /**
     * 实现命令： LPUSH key value, 将一个值value 插入到列表key的表头
     * @param key
     * @param value
     * @return 返回插入后列表的长度
     */
    public Long lpush(String key, String value){
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 实现命令： LPOP key, 移除指定key并返回列表key的头元素
     * @param key
     * @return 列表key的头元素
     */
    public String lpop(String key){
        return redisTemplate.opsForList().leftPop(key);
    }

    public Long rpush(String key, String value){
        return redisTemplate.opsForList().rightPush(key, value);
    }


}
