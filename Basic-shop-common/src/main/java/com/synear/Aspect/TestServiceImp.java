package com.synear.Aspect;


import org.springframework.stereotype.Component;

@Component
public class TestServiceImp implements TestService {

    @Override
    public void toMyWife() {
        System.out.println("每周一件事");
        System.out.println("对老婆说：我爱你！");
        System.out.println("抱住亲亲你");
    }

}
