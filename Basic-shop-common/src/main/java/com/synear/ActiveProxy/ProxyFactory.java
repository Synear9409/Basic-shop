package com.synear.ActiveProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {

    private Object target;   // 需要被代理的对象

    public ProxyFactory(Object target) {
        this.target = target;
    }

    // 生成代理对象
    public Object getProxyInstance() {

        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("客户： 我想买房~");
                        // 执行被代理对象定义的方法
                        method.invoke(target, args);
                        System.out.println("客户： 好的，那中介费我就白嫖了~");
                        return null;
                    }
                });
    }

}
