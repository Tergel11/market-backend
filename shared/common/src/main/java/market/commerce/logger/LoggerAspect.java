package market.commerce.logger;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Tergel
 */
@Slf4j
@Aspect
@Component
public class LoggerAspect {

    @Around("@annotation(market.commerce.logger.LogExecutionDuration)"
            + " || @within(market.commerce.logger.LogExecutionDuration)")
    public Object logDuration(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            log.info("{} executed in {} ms",
                    joinPoint.getSignature().toShortString(),
                    System.currentTimeMillis() - start);
        }
    }
}
