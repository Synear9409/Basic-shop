package com.synear.BloomFilter;


import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.synear.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 布隆过滤器场景运用 ---- redis缓存穿透
 */
@Component
public class BloomFilterUtil {

    private static int total = 1000000;  // 预计要插入多少数据

    private static double fpp = 0.001;   //期望的误判率   实际情况看服务器配置如何再进行赋比较合适的值

    // 考虑空间与时间而牺牲误判率
    private static BloomFilter<Integer> bf = BloomFilter.create(Funnels.integerFunnel(), total, fpp);


    @Autowired
    private RedisUtil redisUtil;

    /**
     * 在使用布隆过滤器前，把数据库的value对应的key《---即<id>或者其他 【看接口根据什么查数据的】---》
     * 存储到布隆过滤器里！！！
     */

    public String get(String key) {

        String value = redisUtil.get(key);

        if (value == null) {
            // redis中没有该缓存
            if (!bf.mightContain(Integer.valueOf(key))) {
                // 布隆过滤器中也没有，直接返回null
                return null;
            } else {
                // 布隆过滤器中查到，不代表就有，所以将其存进redis，同样也可以避免缓存穿透
//                value = db.get(key);    //这一步从数据库查，具体代码根据业务变
                redisUtil.set(key, value);
            }
        }
        return value;
    }

}
