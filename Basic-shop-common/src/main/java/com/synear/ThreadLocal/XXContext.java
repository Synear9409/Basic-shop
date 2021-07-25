package com.synear.ThreadLocal;

import java.util.HashMap;
import java.util.Map;


/**
 * 一次发货操作：会根据入参，进行组件化、流程编排话。
 * 那么入参会被各个地方用到，而且有些流程组件是异步的（类似 new thread 操作的）。
 * 这时候可以定一个 XXContext 上下文：
 */
public class XXContext {

    private static ThreadLocal<Map<Class<?>, Object>> context = new InheritableThreadLocal<>();

    /**
     * 把参数设置到上下文的Map中
     */
    public static void put(Object obj) {
        Map<Class<?>, Object> map = context.get();
        if (map == null) {
            map = new HashMap<>();
            context.set(map);
        }
        if (obj instanceof Enum) {
            map.put(obj.getClass().getSuperclass(), obj);
        } else {
            map.put(obj.getClass(), obj);
        }
    }

    /**
     * 从上下文中，根据类名取出参数
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> c) {
        Map<Class<?>, Object> map = context.get();
        if (map == null) {
            return null;
        }
        return (T) map.get(c);
    }

    /**
     * 清空ThreadLocal的数据
     */
    public static void clean() {
        context.remove();
    }
}
