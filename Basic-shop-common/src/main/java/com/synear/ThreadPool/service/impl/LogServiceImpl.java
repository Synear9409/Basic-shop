package com.synear.ThreadPool.service.impl;

import com.synear.ThreadPool.dao.LogDao;
import com.synear.ThreadPool.pojo.LogBean;
import com.synear.ThreadPool.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogDao logDao;

    @Override
    public void batchInsert(List<LogBean> list) {
        logDao.batchInsert(list);
    }

    @Override
    public void batchInsert2(LogBean log) {
        logDao.batchInsert2(log);
    }
}
