package fr.abes.item.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    private Integer numDemande;

    @Around("execution(* fr.abes.item.traitement.*.*(..))")
    public Object logMethodProcess(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getName();
        Object[] args = joinPoint.getArgs();

       // log.info("Entering method {} in class {} ", methodName, className);
        //log.info("Methods arguments : {}", Arrays.toString(args));

        if (className.equals("ProxyRetry") && methodName.equals("saveExemplaire")) {
            joinPoint.getThis();
        }
        Object result = joinPoint.proceed();
        log.info("resultat {}", result);
        return result;
    }
}
