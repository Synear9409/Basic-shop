package com.synear.ActiveProxy;

public class RealSubject implements Subject {

    @Override
    public void buy() {
        // 买房
        System.out.println("中介： 我帮你买~");
    }

}
