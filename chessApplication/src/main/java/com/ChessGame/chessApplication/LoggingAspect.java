package com.ChessGame.chessApplication;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class LoggingAspect {

    // Pointcut for all methods in your WebSocketController
    @Pointcut("execution(* com.ChessGame.chessApplication.WebSocketController.*(..))")
    public void chessControllerMethods() {}

    // Log before a method executes
    @Before("chessControllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        String[] paramNames = codeSignature.getParameterNames();
        Object[] paramValues = joinPoint.getArgs();

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < paramNames.length; i++) {
            params.append(paramNames[i]).append("=").append(paramValues[i]).append("; ");
        }

        System.out.println("🟢 [CALL] " + methodName + " | Args: " + params);
    }

    // Log after a method returns
    @AfterReturning(pointcut = "chessControllerMethods()", returning = "result")
    public void logAfterReturn(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();

        // Limit log length for large arrays like the chess board
        String resultPreview = (result instanceof String[][])
                ? "Board state updated"
                : String.valueOf(result);

        System.out.println("✅ [RETURN] " + methodName + " | Result: " + resultPreview);
    }

    // Log if an exception is thrown
    @AfterThrowing(pointcut = "chessControllerMethods()", throwing = "ex")
    public void logAfterThrow(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        System.err.println("❌ [EXCEPTION] in " + methodName + " | " + ex.getMessage());
        ex.printStackTrace();
    }
}

