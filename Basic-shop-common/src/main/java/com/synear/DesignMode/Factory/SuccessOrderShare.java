package com.synear.DesignMode.Factory;

import org.springframework.stereotype.Component;

@Component
public class SuccessOrderShare implements Share{
    @Override
    public String getShareFunctionType() {
        return ShareFactory.EnumShareType.SUCCESS_ORDER.getName();
    }

    @Override
    public String mainProcess(String shareName) {
        // 一些业务代码
        return shareName;
    }
}
