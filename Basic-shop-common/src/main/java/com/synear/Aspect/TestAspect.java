package com.synear.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TestAspect {

//    @Pointcut("execution(* com.synear.Aspect..*.*(..))")
    @Pointcut(value = "@annotation(com.synear.annotation.TestAnnotation)")
    private void testAspect(){};

    @Around("testAspect()")
    public Object handleTestAspect(ProceedingJoinPoint point) throws Throwable {

        System.out.println("《切面知识》操练....");

        Object result = point.proceed();

        System.out.println("希望老婆能原谅我~~~");

        return result;

    }

}
