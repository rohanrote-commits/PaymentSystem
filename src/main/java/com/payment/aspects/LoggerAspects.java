package com.payment.aspects;

import com.payment.helper.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggerAspects {

    private final RequestUserContext requestUserContext;

    @Around("execution(* com.payment.Payments.CardMethod.*(..))")
    public Object logAroundPaymentMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String username = requestUserContext.getUsername();
        log.info("User: {} | Before method execution: {}", username, joinPoint.getSignature());

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();

        log.info("User: {} | After method execution: {} | Execution time: {} ms",
                username, joinPoint.getSignature(), (end - start));

        return result;
    }
}
