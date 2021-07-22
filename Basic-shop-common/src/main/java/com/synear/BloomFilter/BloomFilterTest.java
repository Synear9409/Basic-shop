package com.synear.BloomFilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

/**
 * 测试布隆过滤器
 *
 * @desc 场景运用
 * 1、cerberus在收集监控数据的时候, 有的系统的监控项量会很大,
 * 需要检查一个监控项的名字是否已经被记录到db过了,如果没有的话就需要写入db.
 *
 * 2、垃圾邮件过滤。如果用哈希表，每存储一亿个 email地址，
 * 就需要 1.6GB的内存（用哈希表实现的具体办法是将每一个 email地址对应成一个八字节的信息指纹，
 * 然后将这些信息指纹存入哈希表，由于哈希表的存储效率一般只有 50%，因此一个 email地址需要占用十六个字节。
 * 一亿个地址大约要 1.6GB，即十六亿字节的内存）。因此存贮几十亿个邮件地址可能需要上百 GB的内存。
 * 而Bloom Filter只需要哈希表 1/8到 1/4 的大小就能解决同样的问题。
 *
 * @author Synear
 */
public class BloomFilterTest {

    private static int total = 1000000;  // 预计要插入多少数据

    private static double fpp = 0.001;   //期望的误判率

    // 考虑空间与时间而牺牲误判率
    private static BloomFilter<Integer> bf = BloomFilter.create(Funnels.integerFunnel(), total);

    // 降低误判率，就会花费更多的空间资源和时间
    //private static BloomFilter<Integer> bf = BloomFilter.create(Funnels.integerFunnel(), total, fpp);

    public static void main(String[] args) {
        // 初始化1000000条数据到过滤器中
        for (int i = 0; i < total; i++) {
            bf.put(i);
        }

        // 匹配已在过滤器中的值，是否有匹配不上的
        for (int i = 0; i < total; i++) {
            if (!bf.mightContain(i)) {
                System.out.println("有坏人逃脱了~~~");
            }
        }

        // 匹配不在过滤器中的10000个值，有多少匹配出来
        int count = 0;
        for (int i = total; i < total + 10000; i++) {
            if (bf.mightContain(i)) {
                count++;
            }
        }
        System.out.println("误伤的数量：" + count);
    }

}
