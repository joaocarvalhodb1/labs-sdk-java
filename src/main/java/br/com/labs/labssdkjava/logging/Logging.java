package br.com.labs.labssdkjava.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.List;

public class Logging {

    private final Logger logger;

    private Logging(Logger logger) {
        this.logger = logger;
    }

    public static Logging New(String serviceName, Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return new Logging(logger).initializeMDC(serviceName, clazz.getName());
    }

    public void log(org.slf4j.event.Level level, String message, LogContext... contexts) {
        try {
            MDC.clear();
            MDC.put("level", level.toString());

            LogEntry logEntry = createLogEntry(message, Arrays.asList(contexts));
            String jsonLog = logEntry.toJson();

            switch (level) {
                case INFO:
                    logger.info(jsonLog);
                    break;
                case DEBUG:
                    logger.debug(jsonLog);
                    break;
                case ERROR:
                    logger.error(jsonLog);
                    break;
                case WARN:
                    logger.warn(jsonLog);
                    break;
                case TRACE:
                    logger.trace(jsonLog);
                    break;
                default:
                    logger.info(jsonLog);
                    break;
            }
        } finally {
            MDC.clear();
        }
    }

    private LogEntry createLogEntry(String message, List<LogContext> contexts) {
        LogEntry logEntry = new LogEntry();
        logEntry.setMessage(message);
        logEntry.setClazz(Thread.currentThread().getStackTrace()[3].getClass()); // Aqui, usamos o índice 3 para pegar a classe correta
        logEntry.setContexts(contexts);
        return logEntry;
    }

    private Logging initializeMDC(String serviceName, String className) {
        MDC.put("service", serviceName);
        MDC.put("class", className);
        return this;
    }

    public void Info(String message, LogContext... contexts) {
        log(org.slf4j.event.Level.INFO, message, contexts);
    }

    public void Debug(String message, LogContext... contexts) {
        log(org.slf4j.event.Level.DEBUG, message, contexts);
    }

    public void Error(String message, LogContext... contexts) {
        log(org.slf4j.event.Level.ERROR, message, contexts);
    }

    public void Warn(String message, LogContext... contexts) {
        log(org.slf4j.event.Level.WARN, message, contexts);
    }

    public void Trace(String message, LogContext... contexts) {
        log(org.slf4j.event.Level.TRACE, message, contexts);
    }
}
