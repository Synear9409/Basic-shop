package com.synear.DesignMode.Factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShareFactory {

    @Autowired
    @Qualifier(value = "defeatOrderShare")
    private List<Share> shareFunctionList;


    /**
     * 根据分享类型获取对应的分享处理方式
     * @param type
     * @return
     */
    public Share getShareFunction(String type) {

        for (Share shareFunction : shareFunctionList) {
            if (shareFunction.getShareFunctionType().equals(type)) {
                return shareFunction;
            }
        }
        return null;
    }

    public enum EnumShareType{
        SUCCESS_ORDER("successOrder"),
        DEFEAT_ORDER("defeat");

        private String name;

        EnumShareType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Override
    public String toString() {
        return "ShareFactory{" +
                "shareFunctionList=" + shareFunctionList +
                '}';
    }
}
