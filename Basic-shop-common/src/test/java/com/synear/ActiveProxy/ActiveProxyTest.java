package com.synear.ActiveProxy;

class ActiveProxyTest {

    public static void main(String[] args) {

        RealSubject realSubject = new RealSubject();
        Subject proxyInstance = (Subject) new ProxyFactory(realSubject).getProxyInstance();

        System.out.println(realSubject.getClass());
        System.out.println(proxyInstance.getClass());

        // 代理对象执行buy
        proxyInstance.buy();

    }

}
