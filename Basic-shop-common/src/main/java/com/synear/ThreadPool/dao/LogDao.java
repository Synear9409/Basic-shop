package com.synear.ThreadPool.dao;

import com.synear.ThreadPool.pojo.LogBean;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Mapper  //用这个注解 就不用再启动类加mapperScan了
@Repository
public interface LogDao {

    /**
     *  批量进行日志插入
     * @param list  测试日志集合
     */
    void batchInsert(List<LogBean> list);

    /**
     * 单条插入 -- 另一种思路
     * @param log
     */
    void batchInsert2(@Param("log") LogBean log);
//    void batchInsert2(LogBean log);

}
