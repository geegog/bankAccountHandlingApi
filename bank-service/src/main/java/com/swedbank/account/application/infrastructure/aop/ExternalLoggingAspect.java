package com.swedbank.account.application.infrastructure.aop;

import com.swedbank.account.application.integration.LogIntegration;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class ExternalLoggingAspect {

    public static LogIntegration logIntegration;

    @Before("@annotation(simulateExternalLog)")
    public void logToExternalSystem(SimulateExternalLog simulateExternalLog) {
        try {
            logIntegration.logSimulatorCall();
        } catch (Exception e) {
            // If the external logger is down, we abort the entire operation defensively
            throw new IllegalStateException("External logging simulation failed. Debiting aborted.", e);
        }
    }

}
