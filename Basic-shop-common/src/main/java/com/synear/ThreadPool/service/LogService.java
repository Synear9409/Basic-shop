package com.synear.ThreadPool.service;

import com.synear.ThreadPool.pojo.LogBean;

import java.util.List;

public interface LogService {

    /**
     *  批量进行日志插入
     * @param list
     */
    void batchInsert(List<LogBean> list);


    void batchInsert2(LogBean log);

}
