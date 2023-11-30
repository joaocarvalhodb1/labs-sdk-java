package br.com.labs.labssdkjava.logging;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class LogEntry {
    private String message;
    private List<LogContext> contexts;
    private Class<?> clazz;

    public LogEntry() {
    }

    public LogEntry(String message, List<LogContext> contexts, Class<?> clazz) {
        this.message = message;
        this.contexts = contexts;
        this.clazz = clazz;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<LogContext> getContexts() {
        return contexts;
    }

    public void setContexts(List<LogContext> contexts) {
        this.contexts = contexts;
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
}