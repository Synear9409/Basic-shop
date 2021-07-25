package com.synear.ThreadLocal;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description: SimpleDateFormat就一份，不浪费资源。
 *
 * 最后你会觉得每个线程都new一个simpleDateFormat，跟直接在方法内部new没区别啊，
 * 其实不然，1个请求进来是一个线程，他可能贯穿了N个方法，
 * 那这N个方法则可以直接使用该线程的simpleDateFormat，直接new会产生N个SimpleDateFormat对象，
 * 而用ThreadLocal的话只会产生一个对象，一个线程一个。
 */
public class ThreadSimpleDateFormat {

//    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static String dateToStr(int millisSeconds) {
        Date date = new Date(millisSeconds);
        SimpleDateFormat simpleDateFormat = ThreadSafeFormatter.dateFormatThreadLocal.get();
        return simpleDateFormat.format(date);
    }

    private static final ExecutorService executorService = Executors.newFixedThreadPool(100);

    public static void main(String[] args) {
        for (int i = 0; i < 10000; i++) {
            int j = i;
            executorService.execute(() -> {
                String date = dateToStr(j * 1000);
                // 从结果中可以看出是线程安全的，时间没有重复的。
                System.out.println(date);
            });
        }
        executorService.shutdown();
    }
}

class ThreadSafeFormatter {
    /*public static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        }
    };*/
    public static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
}
