package com.synear.Aspect;


import com.synear.annotation.TestAnnotation;
import org.springframework.stereotype.Component;

@Component
public class TestServiceImp implements TestService {

    @Override
    @TestAnnotation
    public void toMyWife() {
        System.out.println("每周一件事");
        System.out.println("对老婆说：我爱你！");
        System.out.println("抱住亲亲你");
    }

}
