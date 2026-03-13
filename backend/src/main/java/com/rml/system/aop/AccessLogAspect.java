package com.rml.system.aop;

import com.rml.system.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AccessLogAspect {

    private final LogService logService;

    @Pointcut("execution(* com.rml.system.service.UserService.authenticate(..))")
    public void authenticatePointcut() {}

    @Around("authenticatePointcut()")
    public Object logAuthenticate(ProceedingJoinPoint pjp) throws Throwable {
        String username = pjp.getArgs().length > 0 ? String.valueOf(pjp.getArgs()[0]) : "unknown";
        String ip = resolveClientIp();
        try {
            Object result = pjp.proceed();
            logService.recordLog(username, ip, "Login success");
            return result;
        } catch (Exception ex) {
            logService.recordLog(username, ip, "Login failed: " + ex.getMessage());
            throw ex;
        }
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
