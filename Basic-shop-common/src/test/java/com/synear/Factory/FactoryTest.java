package com.synear.Factory;

import com.synear.DesignMode.Factory.Share;
import com.synear.DesignMode.Factory.ShareFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class FactoryTest {

    @Autowired
    private ShareFactory shareFactory;

    @Test
    void testFactory() {
//        System.out.println(shareFactory);
        Share shareFunction = shareFactory.getShareFunction(ShareFactory.EnumShareType.SUCCESS_ORDER.getName());
        String process = shareFunction.mainProcess("Success!");
        System.out.println(process);
    }

}
