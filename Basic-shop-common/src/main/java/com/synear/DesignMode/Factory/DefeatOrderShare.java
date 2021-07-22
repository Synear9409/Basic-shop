package com.synear.DesignMode.Factory;

import org.springframework.stereotype.Component;



@Component
public class DefeatOrderShare implements Share {
    @Override
    public String getShareFunctionType() {
        return ShareFactory.EnumShareType.DEFEAT_ORDER.getName();
    }

    @Override
    public String mainProcess(String shareName) {
        return shareName;
    }

}
